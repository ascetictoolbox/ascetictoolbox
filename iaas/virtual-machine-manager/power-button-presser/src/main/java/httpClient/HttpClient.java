package httpClient;

import com.squareup.okhttp.*;

import java.io.IOException;

public class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    public HttpClient() { }

    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String put(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(JSON, ""))
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}