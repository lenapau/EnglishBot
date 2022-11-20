package scenario;

import scenario.training.InputTrainingData;
import scenario.training.OutputTrainingData;
import scenario.training.TrainingState;
import scenario.translate.EnglishLanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.YandexTranslator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TrainingScenario implements Scenario<InputTrainingData, OutputTrainingData> {

    private final YandexTranslator translator;


    String TRAINING_FLAG = "\uD83C\uDFC6";


    private String currentTargetWord = null;

    public TrainingScenario(YandexTranslator translator) {
        this.translator = translator;
    }



    private List<String> getLinesFromFile() throws IOException {
        try  {
            Supplier<Stream<String>> lines = getLinesStream();
            int amount = (int) lines.get().count();
            List<Integer> randomNumbers = getRandomNumbers(amount-1);
            String string1 = getWordFromLine(0, randomNumbers, lines);
            String string2 = getWordFromLine(1,randomNumbers, lines);
            String string3 = getWordFromLine(2,randomNumbers, lines);
            return Arrays.asList(string1, string2, string3);
        } catch (IOException e) {
            System.out.println(e);
            throw e;
        }
    }

    private Supplier<Stream<String>> getLinesStream() throws IOException {
        return  () -> {
            try {
                return Files.lines(Paths.get("C:\\Users\\Лена\\IdeaProjects\\echobot2\\target\\classes\\data\\english_words.json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private String getWordFromLine(int index, List<Integer> list, Supplier<Stream<String>> lines) {
        return lines.get().skip(list.get(index))
                .findFirst()
                .get()
                .trim()
                .replace(",", "")
                .replace("\"", "");
    }

    @Override
    public String getName() {
        return String.format("Тренировка %s", TRAINING_FLAG);
    }

    @Override
    public OutputTrainingData execute(InputTrainingData data) throws Exception {
        if (data.state() == TrainingState.START) {
            return handleStartState();
        }
        if (Objects.equals(currentTargetWord, data.message())) {
            return new OutputTrainingData(new ArrayList<>(), "Правильно!");
        }
        return new OutputTrainingData(new ArrayList<>(), String.format("Неправильно:( Верный ответ: %s", currentTargetWord));
    }

    private OutputTrainingData handleStartState() throws Exception{
        List<String> variants = getLinesFromFile();
        var random = ThreadLocalRandom.current();
        String targetWord = variants.get(random.nextInt(variants.size() - 1));
        currentTargetWord = targetWord;
        String translatedTargetWord = translator.translate(new EnglishLanguage(), new RussianLanguage(), targetWord);
        return new OutputTrainingData(variants, String.format("Как перевести это слово - %s?", translatedTargetWord));
    }

    private List<Integer> getRandomNumbers(int listSize) {
        List<Integer> result = new ArrayList<>();
        var random = ThreadLocalRandom.current();
        while (result.size() != 3) {
            Integer number = random.nextInt(listSize);
            if (!result.contains(number)) {
                result.add(number);
            }
        }

        return result;
    }

}
