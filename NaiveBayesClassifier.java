package twitter.svm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter.sentiment.tables.SentanceManager;

import twitter.svm.SentimentClass;

public class NaiveBayesClassifier {

    public String strPositive = "";
    public String strNegative = "";

    private Map<String, List<String>> dataSet;

    private Map<String, Map<String, Long>> frequencyMap;

    public NaiveBayesClassifier() {
        dataSet = new HashMap<String, List<String>>();

        dataSet.put(SentimentClass.POSITIVE.name(), initPositiveData());
        dataSet.put(SentimentClass.NEGATIVE.name(), initNegativeData());

        this.frequencyMap = new HashMap<String, Map<String, Long>>();
    }

    private List<String> initPositiveData() {
        List<String> positive = new ArrayList<>();

        ResultSet rs = SentanceManager.getPolarityPositive();
        try {
            while (rs.next()) {
                positive.add(rs.getString(1));
                //System.out.println(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(NaiveBayesClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        positive.add("i");
        positive.add("love");
        positive.add("you");
        positive.add("like");
        positive.add("awesome");
        positive.add("love");
        positive.add("good");
        positive.add("better");
        positive.add("awesome");
        positive.add("nice");
        positive.add("book");/*
        positive.add("not bad");
        positive.add("good");
        positive.add("very good");
        positive.add("easy");
        positive.add("extremely satisfied");
        positive.add("fast");
        positive.add("wonderful");
        positive.add("wonderful set");
        positive.add("a wonderful");
        positive.add("am extremely");
        positive.add("satisfied with");
        positive.add("is good");*/

        return positive;
    }

    private List<String> initNegativeData() {
        List<String> negative = new ArrayList<>();
        ResultSet rs = SentanceManager.getPolarityNegative();
        try {
            while (rs.next()) {
                negative.add(rs.getString(1));
                //System.out.println(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(NaiveBayesClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        negative.add("hate");
        negative.add("you");
        negative.add("loathe");
        negative.add("dislike");
        negative.add("hate");
        negative.add("unlike");
        negative.add("no");
        negative.add("book");
        negative.add("this");
        negative.add("is");
        negative.add("not");
        negative.add("bad");/*
        negative.add("not good");
        negative.add("very");
        negative.add("very bad");
        negative.add("camera");
        negative.add("canon");
        negative.add("i");
        negative.add("recently");
        negative.add("purchased");
        negative.add("the");
        negative.add("with");
        negative.add("g3");
        negative.add("to");
        negative.add("picture");
        negative.add("in");
        negative.add("of");
        negative.add("my");
        negative.add("extremely");
        negative.add("first");
        negative.add("digital");
        negative.add("bought");
        negative.add("about");
        negative.add("a");
        negative.add("ago");
        negative.add("has");
        negative.add("set");
        negative.add("features");
        negative.add("set of");
        negative.add("i recently");
        negative.add("of features");
        negative.add("has a");
        negative.add("camera has");
        negative.add("the camera");
        negative.add("recently purchased");
        negative.add("purchased the");
        negative.add("the canon");
        negative.add("canon powershot");
        negative.add("powershot g3");
        negative.add("g3 and");
        negative.add("and am");
        negative.add("with the");
        negative.add("the purchase");
        negative.add("is not");
        negative.add("camera is");
        negative.add("really");
        negative.add("really have");
        negative.add("have");
        negative.add("n`t");
        negative.add("have n`t");
        negative.add("is bad");
        negative.add("camera features");*/

        return negative;
    }

    public void generateFrequencyMap() {
        this.frequencyMap.put(SentimentClass.POSITIVE.name(),
                this.generateFrequencyMapType(SentimentClass.POSITIVE));
        this.frequencyMap.put(SentimentClass.NEGATIVE.name(),
                this.generateFrequencyMapType(SentimentClass.NEGATIVE));
    }

    public Map<String, Map<String, Long>> getFrequencyMap() {
        return this.frequencyMap;
    }

    private Map<String, Long> generateFrequencyMapType(SentimentClass sentType) {
        String[] words = (String[]) this.dataSet.get(sentType.name()).
                toArray(new String[this.dataSet.get(sentType.name()).size()]);

        Map<String, Long> frequencyMap = this.getFrequencyMap(words);
        return frequencyMap;

    }

    private Map<String, Long> getFrequencyMap(String[] strings) {
        HashMap<String, Long> frequencyMap = new HashMap<String, Long>();
        for (String s : strings) {
            if (!frequencyMap.containsKey(s)) {
                frequencyMap.put(s, (long) 1);
            } else {
                frequencyMap.put(s, frequencyMap.get(s) + 1);
            }
        }
        return frequencyMap;
    }

    public SentimentClass classify(String text) {
        String[] splitted = text.split("\\s+");
        double positiveCount = this.dataSet.get(SentimentClass.POSITIVE.name()).size();
        double negativeCount = this.dataSet.get(SentimentClass.NEGATIVE.name()).size();
        double positiveSentimentProbability = 1;
        double negativeSentimentProbablity = 1;

        //Map<String, Long> frequencyMap = this.getFrequencyMap(splitted);
        for (String s : splitted) {
            if (s.equals("")) {

            } else {
                positiveSentimentProbability *= this.calculateProbability1(s,
                        SentimentClass.POSITIVE);
                negativeSentimentProbablity *= this.calculateProbability1(s,
                        SentimentClass.NEGATIVE);
            }
        }

        positiveSentimentProbability *= (positiveCount / (positiveCount + negativeCount));
        negativeSentimentProbablity *= (negativeCount / (positiveCount + negativeCount));

        strPositive = positiveSentimentProbability + "";
        System.out.println("positive probablity : " + positiveSentimentProbability);
        strNegative = negativeSentimentProbablity + "";
        System.out.println("negative probablity : " + negativeSentimentProbablity);

        SentimentClass sentType = (positiveSentimentProbability > negativeSentimentProbablity)
                ? SentimentClass.POSITIVE : SentimentClass.NEGATIVE;

        System.out.println(sentType + "sentType");
        return sentType;
    }

    public double calculateProbability(String word, SentimentClass sentType) {
        Map<String, Long> fm = this.frequencyMap.get(sentType.name());
        System.out.println(fm.get(word));
        double totalCount = this.dataSet.get(sentType.name()).size();
        double wordCount = (this.frequencyMap.get(sentType.name())).get(word);
        return wordCount / totalCount;
    }

    public double calculateProbability1(String word, SentimentClass sentType) {
        double totalCount = this.dataSet.get(sentType.name()).size();
        double wordCount = 0;

        for (String s : this.dataSet.get(sentType.name())) {
            if (word.equals(s)) {
                wordCount += 1;
            }
        }
        return wordCount / totalCount;
    }
}
