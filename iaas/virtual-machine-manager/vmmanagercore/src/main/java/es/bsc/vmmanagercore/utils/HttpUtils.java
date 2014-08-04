package es.bsc.vmmanagercore.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;

/**
 * This helper class contains auxiliary methods to work with HTTP requests.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es).
 *
 */
public class HttpUtils {

    /**
     * Builds a URI with format: scheme:://host:port/path .
     *
     * @param scheme Scheme of the URI (HTTP, HTTPS, etc.).
     * @param host Host of the URI.
     * @param path Path of the URI.
     * @return The URI built.
     */
    public static URI buildURI(String scheme, String host, int port, String path) {
        URI uri = null;
        try {
            uri = new URIBuilder().setScheme(scheme).setHost(host).setPort(port).setPath(path).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
    }

    /**
     * Builds an HTTP Request.
     *
     * @param methodType Type of the request (GET, POST, etc.).
     * @param uri URI of the request.
     * @param header Headers of the request.
     * @param entity Entity of the request.
     * @param filePath Path of a file.
     * @return The HTTP Request built.
     */
    public static HttpRequestBase buildHttpRequest(String methodType, URI uri, Map<String,
            String> header, String entity, String filePath) {
        HttpRequestBase request;

        //instantiate the request according to its type (GET, POST...)
        switch (methodType) {
            case "GET":
                request = new HttpGet(uri);
                break;
            case "POST":
                request = new HttpPost(uri);
                break;
            case "DELETE":
                request = new HttpDelete(uri);
                break;
            default:
                request = null;
                break;
        }

        if (request == null) {
            throw new IllegalArgumentException("Method not supported.");
        }

        //set the headers of the request
        for (Map.Entry<String, String> entry : header.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }

        //if the type of the request is POST, set the entity of the request
        if (methodType.equals("POST")) {
            if (filePath != null) {
                Path path = FileSystems.getDefault().getPath(filePath);
                byte[] fileData = new byte[0];
                try {
                    fileData = Files.readAllBytes(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((HttpPost) request).setEntity(new ByteArrayEntity(fileData));
            }
            else if (!entity.equals("")) {
                try {
                    ((HttpPost) request).setEntity(new StringEntity(entity));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return request;
    }

    /**
     * Gets the response of an HTTP request.
     * 
     * @param request The HTTP request from which we want to obtain the response.
     * @return The response of the HTTP request
     */
    public static String getHttpResponse(HttpRequestBase request) {
        HttpClient httpclient = HttpClients.createDefault();
        String responseContent = "";
        try {
            HttpResponse response = httpclient.execute(request);
            if (response.getEntity() != null) {
                responseContent = IOUtils.toString(response.getEntity().getContent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseContent;
    }

    /**
     * Builds an HTTP Request and returns its response.
     *
     * @param methodType Type of the request (GET, POST, etc.).
     * @param uri URI of the request.
     * @param header Headers of the request.
     * @param entity Entity of the request.
     * @return @return The response of the HTTP request
     */
    public static String executeHttpRequest(String methodType, URI uri, Map<String, String> header, String entity,
            String filePath) {
        return getHttpResponse(buildHttpRequest(methodType, uri, header, entity, filePath));
    }
}
