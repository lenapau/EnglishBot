package scenario.translate;

public class EnglishLanguage implements ILanguage{

    /**
     Сущность, предоставляющая язык для Translator
     */
    @Override
    public String getLocale() {
        return "en";
    }
}
