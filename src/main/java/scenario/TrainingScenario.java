package scenario;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scenario.training.InputTrainingData;
import scenario.training.OutputTrainingData;
import scenario.training.TrainingState;
import scenario.translate.EnglishLanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.YandexTranslator;

import java.io.*;
import java.net.URL;
import java.util.*;

public class TrainingScenario implements IScenario<InputTrainingData, OutputTrainingData> {

    private List<String> englishWords;

    private YandexTranslator translator;


    String TRAINING_FLAG = "\uD83C\uDFC6";

    private final Random random = new Random();

    private String currentTargetWord = null;

    public TrainingScenario(YandexTranslator translator) {
        this.translator = translator;
        try {
            englishWords = getListFromFile("data/english_words.json");
        } catch (IOException | ParseException e) {
            englishWords = new ArrayList<>();
            System.out.println(e.getMessage());
        }
    }

    private List<String> getListFromFile(String filename) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        StringBuilder out = new StringBuilder();
        InputStream inputStream = TrainingScenario.class
                .getClassLoader()
                .getResourceAsStream(filename);


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        }

        JSONArray jsonArray = (JSONArray) parser.parse(out.toString());
        List<String> words = new ArrayList<>();
        for (Object o : jsonArray) {
            String word = (String) o;
            words.add(word);
        }
        return words;
    }

    @Override
    public String getName() {
        return "Тренировка" + TRAINING_FLAG;
    }

    @Override
    public OutputTrainingData execute(InputTrainingData data) throws Exception {
        if (data.state() == TrainingState.START) {
            List<Integer> numbers = getRandomNumbers(englishWords.size() - 1, 3);
            List<String> variants = Arrays.asList(
                    englishWords.get(numbers.get(0)),
                    englishWords.get(numbers.get(1)),
                    englishWords.get(numbers.get(2))
            );
            String targetWord = variants.get(random.nextInt(variants.size() - 1));
            currentTargetWord = targetWord;
            String translatedTargetWord = translator.translate(new EnglishLanguage(), new RussianLanguage(), targetWord);
            return new OutputTrainingData(variants, "Как перевести это слово - " + translatedTargetWord + "?");
        }

        if (Objects.equals(currentTargetWord, data.message())) {
            return new OutputTrainingData(new ArrayList<>(), "Правильно!");
        }

        return new OutputTrainingData(new ArrayList<>(), "Неправильно:( Верный ответ: " + currentTargetWord);
    }

    private List<Integer> getRandomNumbers(int listSize, int count) {
        List<Integer> result = new ArrayList<Integer>();

        while (result.size() != 3) {
            Integer number = random.nextInt(listSize);
            if (!result.contains(number)) {
                result.add(number);
            }
        }

        return result;
    }
}
