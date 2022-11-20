package scenario.translate;

public class EnglishLanguage implements Language {

    /**
     Сущность, предоставляющая язык для Translator
     */
    @Override
    public String getLocale() {
        return "en";
    }
}
