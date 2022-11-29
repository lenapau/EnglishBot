package org.example;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import scenario.document.DocumentData;
import scenario.training.InputTrainingData;
import scenario.training.OutputTrainingData;
import scenario.training.TrainingState;
import scenario.translate.EnglishLanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.TranslateData;
import scenario.translate.YandexTranslator;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;


public class MainBot extends AbilityBot {

    private final YandexTranslator translator = new YandexTranslator();
    private final TranslateScenario translateScenario = new TranslateScenario(translator);
    private final DocumentScenario documentScenario = new DocumentScenario(translator);
    private final TrainingScenario trainingScenario = new TrainingScenario(translator);
    private final List<Scenario> scenarios = Arrays.asList(translateScenario, documentScenario, trainingScenario);

    private final String TRANSLATE_ERROR = "Не удалось перевести слово";

    private final String TRAINING_ERROR = "Ошибка тренировки";

    private final String NO_DOCUMENT_ERROR = "Это не документ:)";
    private final String DOCUMENT_ERROR = "Ошибка документа";

    private static final String START_MESSAGE = """
            Привет!\s
            Этот Бот поможет тебе в обучении английскому языку.\s
            Вы можете перевести слово или текст с помощью кнопки Перевод\uD83C\uDDEC\uD83C\uDDE7,\s
            Тренироваться в запоминании слов с помощью кнопки Тренировка\uD83C\uDFC6, \s
            А также перевести содержимое Вашего документа с помощью кнопки Документ\uD83D\uDCDD""";

    protected MainBot(String botToken, String botUsername) {
        super(botToken, botUsername);
    }


    /**
     * В telegrambots есть подбиблиотека telegramAbilities от того же создателя.
     * Она нужна, чтобы вручную не писать автомат состояний.
     * public Ability задает, что отвечаем на start - action
     */
    @SuppressWarnings("unused")
    public Ability start() {
        return Ability
                .builder()
                .name("start")
                .locality(ALL)
                .privacy(PUBLIC)
                //Отправляем на name("start") такое сообщение
                .action(context -> sendMessage(String.valueOf(context.chatId()), START_MESSAGE, getScenarioButtons()))
                .build();
    }

    public ArrayList<String> getScenarioButtons() {
        ArrayList<String> list = new ArrayList<>();
        for (Scenario scenario : scenarios) {
            list.add(scenario.getName());
        }
        return list;
    }

    private void sendMessage(String chatId, String messageText, List<String> keyboardButtons) { //Отправляем сообщения
        SendMessage message = constructMessageFrom(String.valueOf(chatId),
                messageText, keyboardButtons);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDocument(String chatId, String documentPath) { //Отправляем сообщения
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        File file = new File(documentPath);
        sendDocumentRequest.setDocument(new InputFile(file));
        sendDocumentRequest.setCaption("Переведенный документ: ");

        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage constructMessageFrom(String chatId, String messageText, List<String> keyboardButtons) { //Создаем клавиатуру и кнопки
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
     * В telegrambots есть подбиблиотека telegramAbilities от того же создателя.
     * Она нужна, чтобы вручную не писать автомат состояний.
     * public ReplyFlow задает автомат состояний
     * Мы выполняем action если это не команда и не флаг(который высылается при выборе языка перевода)
     */
    @SuppressWarnings("unused")
    public ReplyFlow translateFlow() {
        ReplyFlow ruReplay = ReplyFlow.builder(db)
                .onlyIf(update -> !update.getMessage().isCommand()
                        && !Objects.equals(update.getMessage().getText(), "\uD83C\uDDF7\uD83C\uDDFA"))
                .action((baseAbilityBot, upd) -> {
                            String answer;
                            try {
                                answer = translateScenario.execute(new TranslateData(upd.getMessage().getText(), new RussianLanguage()));
                            } catch (Exception e) {
                                answer = TRANSLATE_ERROR;
                            }
                            sendMessage(String.valueOf(getChatId(upd)), answer, getScenarioButtons());
                        }
                )
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
                                answer = TRANSLATE_ERROR;
                            }
                            sendMessage(String.valueOf(getChatId(upd)), answer, getScenarioButtons());
                        }
                )
                .onlyIf(update -> !update.getMessage().isCommand()
                        && !Objects.equals(update.getMessage().getText(), TranslateScenario.BRITISH_FLAG))
                //Выполнить экшн если это не команда и не флаг
                .build();

        ReplyFlow enReplayFlow = ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Пришлите слово",
                        List.of()
                ))
                .onlyIf(hasMessageWith(TranslateScenario.BRITISH_FLAG))
                .next(enReplay)
                .build();

        return ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Выберите язык исходного текста",
                        new ArrayList<>(Arrays.asList(TranslateScenario.RUSSIAN_FLAG, TranslateScenario.BRITISH_FLAG))
                ))
                .onlyIf(hasMessageWith(translateScenario.getName()).or(hasMessageWith("/translate")))
                .next(enReplayFlow)
                .next(ruReplayFlow)
                .build();

    }

    @SuppressWarnings("unused")
    public ReplyFlow trainingFlow() {
        ReplyFlow checkFlow = ReplyFlow.builder(db)
                .onlyIf(update -> !update.getMessage().isCommand()
                        && !Objects.equals(update.getMessage().getText(), trainingScenario.getName()))
                .action((baseAbilityBot, upd) -> {
                    try {
                        OutputTrainingData data = trainingScenario.execute(new InputTrainingData(TrainingState.WAITING_FOR_ANSWER, upd.getMessage().getText()));
                        sendMessage(
                                String.valueOf(getChatId(upd)),
                                data.message(),
                                data.variants()
                        );
                    } catch (Exception e) {
                        sendMessage(
                                String.valueOf(getChatId(upd)),
                                TRAINING_ERROR,
                                new ArrayList<>()
                        );
                    }
                    //try {
                    //    silent.send(String.valueOf(trainingScenario.execute(new InputTrainingData(TrainingState.START, null))), getChatId(upd));
                    //} catch (Exception e) {
                    //    throw new RuntimeException(e);
                    //}
                })
                .build();

        return ReplyFlow.builder(db)
                .onlyIf(hasMessageWith(trainingScenario.getName()).or(hasMessageWith("/training")))
                .action((baseAbilityBot, upd) -> {
                    try {
                        OutputTrainingData data = trainingScenario.execute(new InputTrainingData(TrainingState.START, null));
                        sendMessage(
                                String.valueOf(getChatId(upd)),
                                data.message(),
                                data.variants()
                        );
                    } catch (Exception e) {
                        sendMessage(
                                String.valueOf(getChatId(upd)),
                                TRAINING_ERROR,
                                new ArrayList<>()
                        );
                    }
                })
                .next(checkFlow)
                .build();

    }

    @SuppressWarnings("unused")
    public ReplyFlow documentFlow() {
        ReplyFlow ruReplay = ReplyFlow.builder(db)
                .onlyIf(update -> !update.getMessage().isCommand()
                        && !Objects.equals(update.getMessage().getText(), "\uD83C\uDDF7\uD83C\uDDFA"))
                .action((baseAbilityBot, upd) -> {
                            String answer = null;
                            try {
                                if (upd.getMessage().hasDocument()) {
                                    String doc_id = upd.getMessage().getDocument().getFileId();
                                    String doc_name = upd.getMessage().getDocument().getFileName();
                                    String doc_mine = upd.getMessage().getDocument().getMimeType();
                                    long doc_size = upd.getMessage().getDocument().getFileSize();
                                    String getID = String.valueOf(upd.getMessage().getFrom().getId());

                                    Document document = new Document();
                                    document.setMimeType(doc_mine);
                                    document.setFileName(doc_name);
                                    document.setFileSize(doc_size);
                                    document.setFileId(doc_id);

                                    GetFile getFile = new GetFile();
                                    getFile.setFileId(document.getFileId());
                                    try {
                                          org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                                          String documentPath = "./data/userDoc/" + getID + "_" + doc_name;
                                          downloadFile(file, new File(documentPath));
                                          sendDocument(String.valueOf(getChatId(upd)),
                                                  documentScenario.execute(new DocumentData(documentPath, new RussianLanguage(), new EnglishLanguage())));
                                          return;
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    answer = NO_DOCUMENT_ERROR;
                                }

                            } catch (Exception e) {
                                answer = DOCUMENT_ERROR;
                            }
                            sendMessage(String.valueOf(getChatId(upd)), answer, getScenarioButtons());
                        }
                )
                .build();

        ReplyFlow ruReplayFlow = ReplyFlow.builder(db)
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Пришлите документ",
                        new ArrayList<>()
                ))
                .onlyIf(hasMessageWith(TranslateScenario.RUSSIAN_FLAG))
                .next(ruReplay)
                .build();


        ReplyFlow enReplay = ReplyFlow.builder(db)
                .onlyIf(update -> !update.getMessage().isCommand()
                        && !Objects.equals(update.getMessage().getText(), TranslateScenario.BRITISH_FLAG))
                .action((baseAbilityBot, upd) -> {
                            String answer = null;
                            try {
                                if (upd.getMessage().hasDocument()) {
                                    String doc_id = upd.getMessage().getDocument().getFileId();
                                    String doc_name = upd.getMessage().getDocument().getFileName();
                                    String doc_mine = upd.getMessage().getDocument().getMimeType();
                                    long doc_size = upd.getMessage().getDocument().getFileSize();
                                    String getID = String.valueOf(upd.getMessage().getFrom().getId());

                                    Document document = new Document();
                                    document.setMimeType(doc_mine);
                                    document.setFileName(doc_name);
                                    document.setFileSize(doc_size);
                                    document.setFileId(doc_id);

                                    GetFile getFile = new GetFile();
                                    getFile.setFileId(document.getFileId());
                                    try {
                                        org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                                        String documentPath = "./data/userDoc/" + getID + "_" + doc_name;
                                        downloadFile(file, new File(documentPath));
                                        sendDocument(String.valueOf(getChatId(upd)),

                                                documentScenario.execute(new DocumentData(documentPath, new EnglishLanguage(), new RussianLanguage())));
                                        return;
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    answer = NO_DOCUMENT_ERROR;
                                }

                            } catch (Exception e) {
                                answer = DOCUMENT_ERROR;
                            }
                            sendMessage(String.valueOf(getChatId(upd)), answer, getScenarioButtons());
                        }
                )
                //Выполнить экшн если это не команда и не флаг
                .build();

        ReplyFlow enReplayFlow = ReplyFlow.builder(db)
                .onlyIf(hasMessageWith(TranslateScenario.BRITISH_FLAG))
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Пришлите документ",
                        List.of()
                ))
                .next(enReplay)
                .build();

        return ReplyFlow.builder(db)
                .onlyIf(hasMessageWith(documentScenario.getName()).or(hasMessageWith("/document")))
                .action((baseAbilityBot, upd) -> sendMessage(
                        String.valueOf(getChatId(upd)), "Выберите язык исходного текста",
                        new ArrayList<>(Arrays.asList(TranslateScenario.RUSSIAN_FLAG, TranslateScenario.BRITISH_FLAG))
                ))
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
