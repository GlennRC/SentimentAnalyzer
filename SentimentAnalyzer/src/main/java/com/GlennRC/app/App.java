package com.mycompany.app;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        props.setProperty("parse.binaryTrees","true");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        JSONParser parser = new JSONParser();

        String filePath = "Articles/";
        String writeBlogPath = "/Parsed_blogs";
        String writeNewsPath = "/Parsed_news";

        // This is used to narrow down the articles, since there were too many to parse
        // This was modified manually
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date after = cal.getTime();

        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        Date before = cal.getTime();

        try {
            FileWriter fileNews = new FileWriter(writeNewsPath, true);
            FileWriter fileBlog = new FileWriter(writeBlogPath, true);

            int i = 0;
            int start = 10000;
            int end = 10000;
            int count = 0;
            int i_b = start;
            int i_n = start;

            while (count < start + end) {
                i++;
                if(i%100 == 0)
                    System.out.println(Integer.toString(i));

                String indexb = Integer.toString(i_b);
                String indexn = Integer.toString(i_n);
                String blogName = "blogs_" + ("0000000" + indexb).substring(indexb.length()) + ".json";
                String newsName = "news_" + ("0000000" + indexn).substring(indexn.length()) + ".json";

                try {
                    JSONObject b = (JSONObject) parser.parse(new FileReader(filePath + blogName));

                    String dateStr = (String) b.get("published");
                    Date date = javax.xml.bind.DatatypeConverter.parseDateTime(dateStr).getTime();



                    if (date.after(after) && date.before(before)) {
                        String text = (String) b.get("text");
                        Float score = calculateSentiment(text, pipeline);

                        fileBlog.write(dateStr + ", " + score + "\n");
                        fileBlog.flush();

                        count++;
                        if (count%50 == 0)
                            System.out.println(Integer.toString(count));
                    }

                } catch (Exception e) {

                }
                i_b++;

                try {
                    JSONObject n = (JSONObject) parser.parse(new FileReader(filePath + newsName));

                    String dateStr = (String) n.get("published");

                    Date  date = javax.xml.bind.DatatypeConverter.parseDateTime(dateStr).getTime();


                    if (date.after(after) && date.before(before)) {
                        String text = (String) n.get("text");

                        Float score = calculateSentiment(text, pipeline);

                        fileNews.write(dateStr + ", " + score.toString() + "\n");
                        fileNews.flush();

                        count++;
                        if (count%50 == 0)
                            System.out.println(Integer.toString(count));
                    }

                } catch (Exception e) {

                }
                i_n++;
            }
            fileBlog.close();
            fileNews.close();

        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    public static Float calculateSentiment(String text, StanfordCoreNLP pipeline) {

        float mainSentiment = 0;

        int longest = 0;
        Annotation annotation = pipeline.process(text);
        for (CoreMap sentence : annotation
                .get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            String partText = sentence.toString();
            if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }

        }

        return new Float(mainSentiment);

    }

}


