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
        Assert.assertEquals("ru", testObject.getString("sourceLanguageCode"));
        Assert.assertEquals("en", testObject.getString("targetLanguageCode"));
        Assert.assertEquals("кот", testObject.getString("texts"));
    }

    @Test
    public void fromEnToRuJSONTest() {
        JSONObject testObject = translator.createJSON(new EnglishLanguage(), new RussianLanguage(), "cat");
        Assert.assertEquals("en", testObject.getString("sourceLanguageCode"));
        Assert.assertEquals("ru", testObject.getString("targetLanguageCode"));
        Assert.assertEquals("cat", testObject.getString("texts"));
    }

    @Test
    public void HTTPPostCreateTest() {
        HttpPost httpPost = translator.createHttpPost(new JSONObject(), "abc");
        Assert.assertEquals(httpPost.getFirstHeader("Authorization").getValue(), "Api-Key abc");
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
        Assert.assertEquals(newTranslator.translate(new RussianLanguage(), new EnglishLanguage(), "кот"), "cat");
        //ломается если вместо cat что-то другое пишу
    }
}