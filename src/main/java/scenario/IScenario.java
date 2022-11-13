package scenario;

public interface IScenario<INPUT, OUTPUT> {
    String getName();

    public OUTPUT execute(INPUT input) throws Exception;
}


