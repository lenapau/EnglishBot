package scenario.translate;

public class TranslateData {
    public String word;
    public ILanguage language;
    public  TranslateData(String word, ILanguage language) {
        this.word = word;
        this.language = language;
    }
}

