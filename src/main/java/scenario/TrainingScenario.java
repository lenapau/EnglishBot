package scenario;

public class TrainingScenario implements IScenario<String, String>{
    @Override
    public String getName() {
        return "Тренировка\uD83C\uDFC6";
    }

    @Override
    public String execute(String s) throws Exception {
        return "";
    }
}
