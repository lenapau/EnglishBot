package scenario.translate;

public class RussianLanguage implements ILanguage{

    /**
     Сущность, предоставляющая язык для Translator
     */
    @Override
    public String getLocale() {
        return "ru";
    }
}
