package scenario;

import scenario.translate.EnglishLanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.TranslateData;
import scenario.translate.YandexTranslator;

import java.io.IOException;

public record TranslateScenario(YandexTranslator translator) implements Scenario<TranslateData, String> {

    public static final String RUSSIAN_FLAG = "\uD83C\uDDF7\uD83C\uDDFA";
    public static final String BRITISH_FLAG = "\uD83C\uDDEC\uD83C\uDDE7";

    @Override
    public String getName() {
        return String.format("Перевод%s", BRITISH_FLAG);
    }

    @Override
    public String execute(TranslateData data) throws Exception {
        if (data.language() instanceof RussianLanguage) {
            return translateFromRussian(data.word());
        }
        if (data.language() instanceof EnglishLanguage) {
            return translateFromEnglish(data.word());
        }
        throw new IllegalStateException("unsupported language");
    }

    private String translateFromEnglish(String text) throws IOException {
        return translator.translate(new EnglishLanguage(), new RussianLanguage(), text);
    }

    private String translateFromRussian(String text) throws IOException {
        return translator.translate(new RussianLanguage(), new EnglishLanguage(), text);
    }
}
