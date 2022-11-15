package scenario.training;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import scenario.translate.TranslateData;

import java.util.ArrayList;
import java.util.List;

public record OutputTrainingData(List<String> variants, String message) {

}

