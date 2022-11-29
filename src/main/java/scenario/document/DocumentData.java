package scenario.document;

import org.telegram.telegrambots.meta.api.objects.Document;
import scenario.translate.Language;

/**
 Класс, предоставляющий данные для перевода документа
 */
public  record DocumentData(String documentPath, Language fromLanguage, Language toLanguage) { }
