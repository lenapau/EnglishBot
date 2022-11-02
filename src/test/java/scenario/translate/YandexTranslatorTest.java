package scenario.translate;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class YandexTranslatorTest extends Mockito {
    private final YandexTranslator translator = new YandexTranslator();

    @Test
    public void fromRuToEnJSONTest() {
        JSONObject testObject = translator.createJSON(new RussianLanguage(), new EnglishLanguage(), "кот");
        Assert.assertEquals(testObject.getString("sourceLanguageCode"), "ru");
        Assert.assertEquals(testObject.getString("targetLanguageCode"), "en");
        Assert.assertEquals(testObject.getString("texts"), "кот");
    }

    @Test
    public void fromEnToRuJSONTest() {
        JSONObject testObject = translator.createJSON(new EnglishLanguage(), new RussianLanguage(), "cat");
        Assert.assertEquals(testObject.getString("sourceLanguageCode"), "en");
        Assert.assertEquals(testObject.getString("targetLanguageCode"), "ru");
        Assert.assertEquals(testObject.getString("texts"), "cat");
    }

    @Test
    public void HTTPPostCreateTest() {
        HttpPost httpPost = translator.createHttpPost(new JSONObject(), "abc");
        Assert.assertEquals("Api-Key abc", httpPost.getFirstHeader("Authorization").getValue());
    }//поменять местами

    @Test
    public void translationToEnglishTest() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpEntityParser httpEntityParser = mock(HttpEntityParser.class);

        when(httpEntityParser.getResult(any(HttpEntity.class))).thenReturn("{\"translations\": [{\"text\": \"cat\"}]}");
        when(httpResponse.getEntity()).thenReturn(mock(HttpEntity.class));
        when(httpClient.execute(any())).thenReturn(httpResponse);

        YandexTranslator newTranslator = new YandexTranslator(httpClient, httpEntityParser);
        Assert.assertEquals("cat", newTranslator.translate(new RussianLanguage(), new EnglishLanguage(), "кот"));
        //ломается если вместо cat что-то другое пишу
    }
}