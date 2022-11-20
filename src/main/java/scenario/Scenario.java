package scenario;

public interface Scenario<INPUT, OUTPUT> {
    String getName();

    public OUTPUT execute(INPUT input) throws Exception;
}


