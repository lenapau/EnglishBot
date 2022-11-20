package scenario.translate;

public class RussianLanguage implements Language {

    /**
     Сущность, предоставляющая язык для Translator
     */
    @Override
    public String getLocale() {
        return "ru";
    }
}
