package lnpx;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;
import eu.fbk.dh.tint.runner.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class TextAnalyzer {

    private static List<String> auxWords;
    private static TintPipeline pipeline;

    private static void loadTintPipeline() {
        try {
            pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String readFile(String path) {
        try (Scanner s = new Scanner(new File(path), "UTF-8");) {
            String text = "";
            while (s.hasNext()) {
                text += s.next() + " ";
            }
            return text;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void loadAuxiliaryWords() {
        try {
            auxWords = new ArrayList<>();
            Scanner d = new Scanner(new File("words.txt"));
            while (d.hasNext()) {
                auxWords.add(d.next());
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private static List<String> getKeywords(String text) throws Exception {

        //Tint doesn't work with these punctuation marks
        text = text.replaceAll("\"", ",").replaceAll("-", ",").replaceAll("”", ",").trim();

        List<String> words = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        Annotation document = pipeline.runRaw(text);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        String word = "";

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String pos = token.get(PartOfSpeechAnnotation.class);
                if (pos.equals("SP")) {
                    word += token.get(TextAnnotation.class) + " ";
                } else {
                    if (!word.isEmpty()) {
                        words.add(word.trim());
                        roles.add("SP");
                    }
                    word = token.get(TextAnnotation.class) + " ";
                    words.add(word.trim());
                    roles.add(pos);
                    word = "";
                }
            }
        }

        for (int i = 0; i < roles.size(); i++) {
            if (!roles.get(i).startsWith("S") && !roles.get(i).startsWith("A")) {
                words.remove(i);
                roles.remove(i);
                i--;
            }
        }
        return words;
    }

    private static List<String> normalizeAndFilter(List<String> keywords) {
        for (int i = 0; i < keywords.size(); i++) {
            String word = keywords.get(i);
            word = word.toLowerCase();
            word = word.replaceAll("[^a-zA-Z\\s+]", "");
            if (auxWords.contains(word)) {
                keywords.remove(i);
                i--;
            }
        }
        return keywords;
    }

    private static Map<String, Integer> countWordsOccurrences(List<String> keywords) {
        Map<String, Integer> dictionary = new HashMap<>();
        for (String word : keywords) {

            if (word.isEmpty()) {
                continue;
            }
            if (dictionary.get(word) == null) {
                dictionary.put(word, 1);
            } else {
                dictionary.put(word, dictionary.get(word) + 1);
            }
        }
        return dictionary;
    }

    private static Map<String, Integer> sortMapByValue(Map<String, Integer> map) {
        Map<String, Integer> sortedMap = map.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap;
    }

    public static Map<String, Integer> keywordAnalysis(String text) throws Exception {

        loadTintPipeline();
        loadAuxiliaryWords();

        List<String> keywords = getKeywords(text);

        Map<String, Integer> wordsCount = sortMapByValue(countWordsOccurrences(normalizeAndFilter(keywords)));

        return wordsCount;
    }

}
