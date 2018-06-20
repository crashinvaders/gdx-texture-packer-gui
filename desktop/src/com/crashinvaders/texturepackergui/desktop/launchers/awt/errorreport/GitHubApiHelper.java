package com.crashinvaders.texturepackergui.desktop.launchers.awt.errorreport;

import com.badlogic.gdx.utils.*;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import okhttp3.HttpUrl;
import org.apache.commons.io.IOUtils;
import org.lwjgl.Sys;

import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static com.crashinvaders.texturepackergui.AppConstants.GITHUB_OWNER;
import static com.crashinvaders.texturepackergui.AppConstants.GITHUB_REPO;

public class GitHubApiHelper implements Closeable {
    private static final int PORT = 20023;
    private static final String BASE_URL = "http://localhost:";
    private static final String AUTH_CALLBACK_PATH = "/auth-github";
    private static final String CALLBACK_URL = BASE_URL + PORT + AUTH_CALLBACK_PATH;
    private static final String INVALID_API_KEY = "Invalid";

    private final String apiSecret;
    private final AuthCallbackHandler authCallbackHandler;
    private final OAuth20Service apiService;
    private final Json json;

    public GitHubApiHelper() throws IOException {
        apiSecret = resolveApiKey();

        authCallbackHandler = new AuthCallbackHandler();

        // Request access to interact with public repos.
        apiService = new ServiceBuilder("466a909f95b8e2789a5e")
                .apiSecret(apiSecret)
                .state("authorized")
                .scope("public_repo")   // Request access to interact with public repos.
                .callback(CALLBACK_URL)
                .build(GitHubApi.instance());

        json = new Json(JsonWriter.OutputType.json);
    }

    @Override
    public void close() throws IOException {
        authCallbackHandler.close();
    }

    /** Beware: there is no timeout for browser GitHub authorization and in case user closed/left
     * authorization page without completing whole process, there will be no feedback in {@link CreateIssueResultHandler}. */
    public void createIssue(final String title, final String body, final CreateIssueResultHandler resultHandler) {
        if (!checkApiKey()) {
            resultHandler.onError(new IllegalStateException("GitHub API key is invalid."));
            return;
        }

        authCallbackHandler.setListener(new AuthCallbackHandler.Listener() {
            @Override
            public void onAuthCodeReceived(String authCode) {
                authCallbackHandler.setListener(null);
                try {
                    String contentJson = json.toJson(new CreateIssueBody(title, body));

                    OAuth2AccessToken accessToken = apiService.getAccessToken(authCode);

                    OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.github.com/repos/"+GITHUB_OWNER+"/"+GITHUB_REPO+"/issues");
                    request.setPayload(contentJson);
                    apiService.signRequest(accessToken, request);
                    Response response = apiService.execute(request);

                    if (response.getCode() != 201) {
                        resultHandler.onError(new IllegalStateException("GitHub returned bad code: " +
                                response.getCode() + "\n" +
                                response.getMessage() + "\n" +
                                response.getBody()));
                    } else {
                        JsonValue jsonRoot = new JsonReader().parse(response.getBody());
                        String issueUrl = jsonRoot.getString("html_url");
                        resultHandler.onSuccess(issueUrl);
                    }
                } catch (IOException | InterruptedException | ExecutionException | OAuthException e) {
                    e.printStackTrace();
                    resultHandler.onError(e);
                }
            }
        });
        Sys.openURL(apiService.getAuthorizationUrl());
    }

    private boolean checkApiKey() {
        return !INVALID_API_KEY.equals(apiSecret);
    }

    public interface CreateIssueResultHandler {
        void onSuccess(String issueUrl);
        void onError(Exception exception);
    }

    static class CreateIssueBody {
        String title;
        String body;

        public CreateIssueBody(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    private static String resolveApiKey() {
        InputStream is = GitHubApiHelper.class.getClassLoader().getResourceAsStream("github-api");
        if (is == null) {
            System.err.println("GitHubApiHelper: Can't find API key resource.");
            return INVALID_API_KEY;
        }
        try {
            String encoded = IOUtils.toString(is, StandardCharsets.UTF_8);
            return Base64Coder.decodeString(encoded);
        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_API_KEY;
        }
    }

    private static class AuthCallbackHandler implements HttpHandler, Closeable {

        private final HttpServer httpServer;
        private Listener listener;

        public AuthCallbackHandler() throws IOException {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext(AUTH_CALLBACK_PATH, this);
            httpServer.setExecutor(null);
            httpServer.start();
        }

        @Override
        public void close() throws IOException {
            httpServer.stop(0);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestUrl = httpExchange.getRequestURI().toString();
            HttpUrl parsedUrl = HttpUrl.parse(BASE_URL + PORT + requestUrl);
            final String authCode = parsedUrl.queryParameter("code");
            final String authState = parsedUrl.queryParameter("state");   //TODO Check if state matches requested one.
            System.out.println("AuthCallbackHandler: Got auth code: " + authCode);

            String response = "<b>GDX Texture Packer:</b><br/>GitHub OAuth2 token has been successfully received.<p/>You can close this page now.";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            // Notify listener.
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAuthCodeReceived(authCode);
                    }
                }
            });
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        public interface Listener {
            void onAuthCodeReceived(String authCode);
        }
    }
}
