package scenario.training;

import java.util.List;

 /**
 сущность которая хранит данные, которые необходимо отправить пользователю во время TrainingScenario
 */
public record OutputTrainingData(List<String> variants, String message) {

}

