package scenario;

import scenario.translate.EnglishLanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.YandexTranslator;

import java.io.IOException;

public class TranslateScenario implements IScenario{

    public static final String RUSSIAN_FLAG = "\uD83C\uDDF7\uD83C\uDDFA";
    public static final String BRITISH_FLAG = "\uD83C\uDDEC\uD83C\uDDE7";
    private final YandexTranslator translator = new YandexTranslator();

    @Override
    public String getName() {
        return "Перевод" + BRITISH_FLAG;
    }

    public String translateFromEnglish(String text) throws IOException {
        return translator.translate(new EnglishLanguage(), new RussianLanguage(), text);
    }

    public String translateFromRussian(String text) throws IOException {
        return translator.translate(new RussianLanguage(), new EnglishLanguage(), text);
    }
}
