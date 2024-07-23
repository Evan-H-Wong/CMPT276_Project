package group8.skyweaver_inventory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CalendarConfig {

    private static final String APPLICATION_NAME = "CMPT276-Project";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // Use only the GMAIL_SEND scope for Gmail API access
    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR, GmailScopes.GMAIL_SEND);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private String clientId = System.getenv("GOOGLE_CLIENT_ID");
    private String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
    private String redirectUri = System.getenv("GOOGLE_REDIRECT_URI");

    @Bean
    public Calendar googleCalendar() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public Gmail googleGmail() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        String credentialsJson = String.format(
                "{ \"installed\": { \"client_id\": \"%s\", \"client_secret\": \"%s\", \"redirect_uris\": [ \"%s\" ] } }",
                clientId, clientSecret, redirectUri);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new StringReader(credentialsJson));
        return new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    @Bean
    public Credential loadCredential() throws IOException, GeneralSecurityException {
        // Replace "default-user" with the appropriate user identifier if needed
        GoogleAuthorizationCodeFlow flow = googleAuthorizationCodeFlow();
        return flow.loadCredential("default-user");
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAuthorizationUrl(String userGmail) throws IOException, GeneralSecurityException {
        GoogleAuthorizationCodeFlow flow = googleAuthorizationCodeFlow();
        return flow.newAuthorizationUrl().setRedirectUri(redirectUri).setState(userGmail).setAccessType("offline").build();
    }

    public Credential exchangeCode(String code, String userGmail) throws IOException, GeneralSecurityException {
        GoogleAuthorizationCodeFlow flow = googleAuthorizationCodeFlow();
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
        return flow.createAndStoreCredential(tokenResponse, userGmail);
    }

    // The previous loadCredential method now remains unchanged and doesn't need @Bean
    public Credential loadCredential(String userGmail) throws IOException, GeneralSecurityException {
        GoogleAuthorizationCodeFlow flow = googleAuthorizationCodeFlow();
        return flow.loadCredential(userGmail);
    }
}
