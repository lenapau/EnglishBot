package org.example;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import scenario.document.DocumentData;
import scenario.translate.EnglishLanguage;
import scenario.translate.RussianLanguage;
import scenario.translate.YandexTranslator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentScenario implements Scenario<DocumentData, String> {

    private final YandexTranslator translator;

    public DocumentScenario(YandexTranslator translator) {
        this.translator = translator;
    }

    String DOCUMENT_FLAG = "\uD83D\uDCDD";

    @Override
    public String getName() {
        return String.format("Документ %s", DOCUMENT_FLAG);
    }

    @Override
    public String execute(DocumentData documentData) throws Exception {
        Document document = new Document(documentData.documentPath());
        String documentText = document.getText();
        String translatedDocumentText = translator.translate(documentData.fromLanguage(), documentData.toLanguage(), documentText);
        document.cleanup();
        // Inisialize a DocumentBuilder
        DocumentBuilder builder = new DocumentBuilder(document);
        // Insert text to the document A start

        builder.moveToDocumentStart();
        builder.write(translatedDocumentText);

        File documentFile = new File(documentData.documentPath());
        String translatedFilePath = documentFile.getParentFile().getAbsolutePath();
        Path filePath = Paths.get(translatedFilePath,  "translating_" + documentFile.getName());
        document.save(filePath.toString());

        return filePath.toString();
    }
}
