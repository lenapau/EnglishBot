package org.example;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import scenario.DocumentScenario;
import scenario.IScenario;
import scenario.TrainingScenario;
import scenario.TranslateScenario;
import scenario.translate.EnglishLanguage;
import scenario.translate.ILanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.TranslateData;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;


public class MainBot extends AbilityBot {
    private final TranslateScenario translateScenario = new TranslateScenario();
    private final DocumentScenario documentScenario = new DocumentScenario();
    private final TrainingScenario trainingScenario = new TrainingScenario();
    private final List<IScenario> scenarios = Arrays.asList(translateScenario, documentScenario, trainingScenario);

    private final String error = "Не удалось перевести слово";

    protected MainBot(String botToken, String botUsername) {
        super(botToken, botUsername);
    }


    /**
    В telegrambots есть подбиблиотека telegramAbilities от того же создателя.
     Она нужна, чтобы вручную не писать автомат состояний.
     public Ability задает, что отвечаем на start - action
     */
    @SuppressWarnings("unused")
    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(context -> sendMessage(String.valueOf(context.chatId()), """
                                Привет!\s
                                Этот Бот поможет тебе в обучении английскому языку.\s
                                Вы можете перевести слово или текст с помощью кнопки Перевод\uD83C\uDDEC\uD83C\uDDE7,\s
                                Тренироваться в запоминании слов с помощью кнопки Тренировка\uD83C\uDFC6, \s
                                А также перевести содержимое Вашего документа с помощью кнопки Документ\uD83D\uDCDD""", //Отправляем на name("start") такое сообщение
                        getScenarioButtons()))
                .build();
    }

    public ArrayList<String> getScenarioButtons() {
        ArrayList<String> list = new ArrayList<>();
        for (IScenario scenario : scenarios) {
            list.add(scenario.getName());
        }
        return list;
    }

    private void sendMessage(String chatId, String messageText, ArrayList<String> keyboardButtons) { //Отправляем сообщения
        SendMessage message = constructMessageFrom(String.valueOf(chatId),
                messageText, keyboardButtons);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage constructMessageFrom(String chatId, String messageText, ArrayList<String> keyboardButtons) { //Создаем клавиатуру и кнопки
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String buttonText : keyboardButtons) {
            KeyboardRow row = new KeyboardRow();
            row.add(buttonText);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    @NotNull
    private Predicate<Update> hasMessageWith(String msg) {
        return upd -> upd.getMessage().getText().equalsIgnoreCase(msg);
    }

    /**
     В telegrambots есть подбиблиотека telegramAbilities от того же создателя.
     Она нужна, чтобы вручную не писать автомат состояний.
     public ReplyFlow задает автомат состояний
     Мы выполняем action если это не команда и не флаг(который высылается при выборе языка перевода)
     */
    @SuppressWarnings("unused")
    public ReplyFlow translateFlow() {
        ReplyFlow ruReplay = ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> {
                            String answer;
                            try {
                                answer = translateScenario.execute(new TranslateData(upd.getMessage().getText(), new RussianLanguage()));
                            } catch (Exception e) {
                                answer = error;
                            }
                            sendMessage(String.valueOf(getChatId(upd)), answer, getScenarioButtons());
                        }
                )
                .onlyIf(update -> !update.getMessage().isCommand() && !Objects.equals(update.getMessage().getText(), "\uD83C\uDDF7\uD83C\uDDFA"))
                .build();

        ReplyFlow ruReplayFlow = ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Пришлите слово",
                        new ArrayList<>()
                ))
                .onlyIf(hasMessageWith(TranslateScenario.RUSSIAN_FLAG))
                .next(ruReplay)
                .build();


        ReplyFlow enReplay = ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> {
                            String answer;
                            try {
                                answer = translateScenario.execute(new TranslateData(upd.getMessage().getText(), new EnglishLanguage()));
                            } catch (Exception e) {
                                answer = error;
                            }
                            sendMessage(String.valueOf(getChatId(upd)), answer, getScenarioButtons());
                        }
                )
                .onlyIf(update -> !update.getMessage().isCommand() && !Objects.equals(update.getMessage().getText(), TranslateScenario.BRITISH_FLAG))
                //Выполнить экшн если это не команда и не флаг
                .build();

        ReplyFlow enReplayFlow = ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Пришлите слово",
                        new ArrayList<>()
                ))
                .onlyIf(hasMessageWith(TranslateScenario.BRITISH_FLAG))
                .next(enReplay)
                .build();

        return ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Выберите язык исходного текста",
                        new ArrayList<>(Arrays.asList(TranslateScenario.RUSSIAN_FLAG, TranslateScenario.BRITISH_FLAG))
                ))
                .onlyIf(
                        hasMessageWith(translateScenario.getName())
                            .or(hasMessageWith("/translate"))
                )
                .next(enReplayFlow)
                .next(ruReplayFlow)
                .build();

    }

    @Override
    public long creatorId() {
        return 0;
    }
    //Абстрактные классы предоставляют базовый функционал для наследников.А производные классы реализуют этот функционал
}
