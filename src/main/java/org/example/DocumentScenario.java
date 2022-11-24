package org.example;

public class DocumentScenario implements Scenario<String, String> {
    @Override
    public String getName() {
        return "Документ\uD83D\uDCDD";
    }

    @Override
    public String execute(String s) throws Exception {
        return "";
    }
}
