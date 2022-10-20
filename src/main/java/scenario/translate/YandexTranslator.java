package scenario.translate;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class YandexTranslator {
    private HttpClient httpClient;
    private HttpEntityParser entityParser;
    private final String API_KEY = "AQVNyZRW5aLLT_aKmRaKFiwsN6k8IFObheNKyBnP";

    public YandexTranslator() {
        httpClient = new DefaultHttpClient();
        entityParser = new HttpEntityParser();
    }

    public YandexTranslator(HttpClient myHttpClient) {
        httpClient = myHttpClient;
        entityParser = new HttpEntityParser();
    }

    public YandexTranslator(HttpClient myHttpClient, HttpEntityParser parser) {
        httpClient = myHttpClient;
        entityParser = parser;
    }

    public String translate(ILanguage fromLanguage, ILanguage toLanguage, String text) {
        JSONObject payload = createJSON(fromLanguage, toLanguage, text);

        HttpPost httpPost = createHttpPost(payload, API_KEY);

        try  {
            HttpResponse response = httpClient.execute(httpPost);
            String result = entityParser.getResult(response.getEntity());
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonText = (JSONObject) jsonObject.getJSONArray("translations").get(0);
            return jsonText.getString("text");
        } catch (Exception error) {
            System.out.println(error);
            return " ";
        }
    }

    @VisibleForTesting
    protected JSONObject createJSON(ILanguage fromLanguage, ILanguage toLanguage, String text) {
        JSONObject payload = new JSONObject();
        payload.put("sourceLanguageCode", fromLanguage.getLocale());
        payload.put("targetLanguageCode", toLanguage.getLocale());
        payload.put("texts", text);
        return payload;
    }

    @VisibleForTesting
    protected HttpPost createHttpPost(JSONObject payload, String apiKey) {
        HttpPost httpPost = new HttpPost("https://translate.api.cloud.yandex.net/translate/v2/translate");
        httpPost.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        httpPost.addHeader("Authorization", "Api-Key " + apiKey);
        httpPost.addHeader("Content-type", "application/json");
        return httpPost;
    }
}
