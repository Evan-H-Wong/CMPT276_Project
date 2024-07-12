package group8.skyweaver_inventory.services;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import group8.skyweaver_inventory.CalendarConfig;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CalendarService {
    private final Map<String, Calendar> userCalendars = new ConcurrentHashMap<>();
    private final CalendarConfig calendarConfig;
    private final UserRepository userRepository;
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private final HttpTransport httpTransport;

    public CalendarService(CalendarConfig calendarConfig, UserRepository userRepository) throws GeneralSecurityException, IOException {
        this.calendarConfig = calendarConfig;
        this.userRepository = userRepository;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    public void setCredential(String email, Credential credential) throws IOException {
        Calendar calendar = new Calendar.Builder(
                httpTransport,
                jsonFactory,
                credential)
                .setApplicationName("CMPT276-Project")
                .build();
        userCalendars.put(email, calendar);
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

    public Calendar getCalendar(String email) throws IOException, GeneralSecurityException {
        if (!userCalendars.containsKey(email)) {
            loadAndSetCredential(email);
        }
        return userCalendars.get(email);
    }

    public List<Event> getEvents(String email) throws IOException, GeneralSecurityException {
        Calendar calendar = getCalendar(email);
        if (calendar != null) {
            Events events = calendar.events().list("primary").execute();
            return events.getItems();
        }
        return List.of();
    }

    public Event addEvent(String email, Event event) throws IOException, GeneralSecurityException {
        Calendar calendar = getCalendar(email);
        if (calendar != null) {
            return calendar.events().insert("primary", event).execute();
        }
        throw new IOException("User calendar not found for email: " + email);
    }

    public void deleteEvent(String email, String eventId) throws IOException, GeneralSecurityException {
        Calendar calendar = getCalendar(email);
        if (calendar != null) {
            calendar.events().delete("primary", eventId).execute();
        } else {
            throw new IOException("User calendar not found for email: " + email);
        }
    }

    public Event updateEvent(String email, String eventId, Event event) throws IOException, GeneralSecurityException {
        Calendar calendar = getCalendar(email);
        if (calendar != null) {
            return calendar.events().update("primary", eventId, event).execute();
        }
        throw new IOException("User calendar not found for email: " + email);
    }
}
