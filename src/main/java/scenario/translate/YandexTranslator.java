package scenario.translate;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class YandexTranslator {
    private final HttpClient httpClient = new DefaultHttpClient();
    private final String API_KEY = "AQVNyZRW5aLLT_aKmRaKFiwsN6k8IFObheNKyBnP";

    public String translate(ILanguage fromLanguage, ILanguage toLanguage, String text) {
        JSONObject payload = new JSONObject();
        payload.put("sourceLanguageCode", fromLanguage.getLocale());
        payload.put("targetLanguageCode", toLanguage.getLocale());
        payload.put("texts", text);

        HttpPost httpPost = new HttpPost("https://translate.api.cloud.yandex.net/translate/v2/translate");
        httpPost.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        httpPost.addHeader("Authorization", "Api-Key " + API_KEY);
        httpPost.addHeader("Content-type", "application/json");

        try  {
            HttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonText = (JSONObject) jsonObject.getJSONArray("translations").get(0);
            return jsonText.getString("text");
        } catch (Exception error) {
            System.out.println(error);
            return " ";
        }
    }
}
