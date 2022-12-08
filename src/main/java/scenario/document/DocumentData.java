package scenario.document;

import scenario.translate.Language;

/**
 Класс, предоставляющий данные для перевода документа
 */
public  record DocumentData(String documentName, String documentPath, Language fromLanguage, Language toLanguage) { }
