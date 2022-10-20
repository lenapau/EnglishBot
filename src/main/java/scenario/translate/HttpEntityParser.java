package scenario.translate;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpEntityParser {

    public String getResult(HttpEntity entity) throws IOException {
        return EntityUtils.toString(entity);
    }
}
