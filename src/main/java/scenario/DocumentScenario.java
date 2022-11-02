package scenario;

import scenario.translate.TranslateData;

public class DocumentScenario implements IScenario<String, String> {
    @Override
    public String getName() {
        return "Документ\uD83D\uDCDD";
    }

    @Override
    public String execute(String s) throws Exception {
        return "";
    }
}
