package scenario;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scenario.training.TrainingState;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingScenario implements IScenario<String, String> {

    private List<String> russianWords;

    private List<String> englishWords;

    private TrainingState state = TrainingState.START;

    public TrainingScenario() {
        try {
            russianWords = getListFromFile("russian_words.json");
        } catch (IOException | ParseException e) {
            russianWords = new ArrayList<>();
            System.out.println(e.getMessage());
        }
        try {
            englishWords = getListFromFile("english_words.json");
        } catch (IOException | ParseException e) {
            englishWords = new ArrayList<>();
            System.out.println(e.getMessage());
        }
    }

    private List<String> getListFromFile(String filename) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(filename));
        List<String> words = new ArrayList<>();
        for (Object o : jsonArray) {
            String word = (String) o;
            words.add(word);
        }
        return words;
    }

    @Override
    public String getName() {
        return "Тренировка\uD83C\uDFC6";
    }

    @Override
    public String execute(String s) throws Exception {
        return "";
    }
}
