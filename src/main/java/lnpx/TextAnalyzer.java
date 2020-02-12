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
    
    private static int ErrCounter = 0;
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

    private static String cleanText(String text){
        String T;
        if(text.length() > 2500){
            T = text.substring(0, 2500);
        }else{
            T = text;
        }
        T = T.replaceAll("[-+?.^:,;«»—’“”()\"]"," ").replaceAll("E'", "").replaceAll("d ", "").replaceAll("quest ", "").replaceAll("nell ", "").replaceAll("dell ", "").replaceAll(" l ", " ").replaceAll("dall ", "").replaceAll("km", "").replaceAll("à", "a");
        int i = 0;
        int AsciiCode;
        char currentChar;
        while(i < T.length()){
            currentChar = T.charAt(i);
            AsciiCode = (int) currentChar;
            if(AsciiCode < 65 || (AsciiCode > 90 && AsciiCode < 97) && AsciiCode > 122){
                T = T.replaceAll(String.valueOf(AsciiCode), "");
            }else{
                if(i>0 && i < T.length()-1 && T.charAt(i-1)==' ' && T.charAt(i+1)==' ')
                    T = T.replaceAll(" " + currentChar + " ", " ");
            }
            i++;
        }
        T = T.replaceAll("\\s+", " ");
        return T;
    }
    
    private static List<String> getKeywords(String text) throws Exception {

        //Tint doesn't work with these punctuation marks
        text = cleanText(text).trim();
        
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

    public static Map<String, Integer> keywordAnalysis(String text){
        if(text == null){
            return null;
        }
        try {
            loadTintPipeline();
            loadAuxiliaryWords();

            List<String> keywords = getKeywords(text);

            Map<String, Integer> wordsCount = sortMapByValue(countWordsOccurrences(normalizeAndFilter(keywords)));

            return wordsCount;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
