package group8.skyweaver_inventory.services;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import group8.skyweaver_inventory.CalendarConfig;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GmailService {

    private final Map<String, Gmail> userGmails = new ConcurrentHashMap<>();
    private final CalendarConfig calendarConfig;
    private final UserRepository userRepository;
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private final HttpTransport httpTransport;

    public GmailService(CalendarConfig calendarConfig, UserRepository userRepository) throws GeneralSecurityException, IOException {
        this.calendarConfig = calendarConfig;
        this.userRepository = userRepository;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    public void setCredential(String email, Credential credential) throws IOException {
        Gmail gmail = new Gmail.Builder(
                httpTransport,
                jsonFactory,
                credential)
                .setApplicationName("CMPT276-Project")
                .build();
        userGmails.put(email, gmail);
    }

    public void loadAndSetCredential(String email) throws IOException, GeneralSecurityException {
        User user = userRepository.findByGmail(email);
        if (user != null && user.getRefreshToken() != null) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(String.format(
                "{ \"installed\": { \"client_id\": \"%s\", \"client_secret\": \"%s\", \"redirect_uris\": [ \"%s\" ] } }",
                calendarConfig.getClientId(), calendarConfig.getClientSecret(), calendarConfig.getRedirectUri()
            )));

            GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                    httpTransport,
                    jsonFactory,
                    user.getRefreshToken(),
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret()
            ).execute();

            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .build();
            
            credential.setAccessToken(tokenResponse.getAccessToken());
            credential.setRefreshToken(tokenResponse.getRefreshToken());

            if (credential.getAccessToken() != null) {
                user.setToken(credential.getAccessToken());
                userRepository.save(user);
                setCredential(email, credential);
            } else {
                throw new IOException("Failed to refresh token for email: " + email);
            }
        } else {
            throw new IOException("Credential not found for email: " + email);
        }
    }

    public Gmail getGmail(String email) throws IOException, GeneralSecurityException {
        if (!userGmails.containsKey(email)) {
            loadAndSetCredential(email);
        }
        return userGmails.get(email);
    }

    public void sendEmail(String email, String to, String subject, String bodyText) throws MessagingException, IOException, GeneralSecurityException {
        Gmail gmail = getGmail(email);
        MimeMessage mimeMessage = createEmail(to, subject, bodyText);
        sendMessage(gmail, "me", mimeMessage);
    }

    private MimeMessage createEmail(String to, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("your-email@gmail.com"));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private void sendMessage(Gmail service, String userId, MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        service.users().messages().send(userId, message).execute();
    }
}
