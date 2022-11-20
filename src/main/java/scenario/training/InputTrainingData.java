package scenario.training;

/**
    сущность которая хранит данные, которые вводит пользователь во время TrainingScenario
 */
public record InputTrainingData(TrainingState state, String message) {}
