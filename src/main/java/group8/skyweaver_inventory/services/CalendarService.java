package group8.skyweaver_inventory.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CalendarService {
    private Calendar calendar;

    public CalendarService(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setCredential(Credential credential) {
        this.calendar = new Calendar.Builder(
                credential.getTransport(),
                credential.getJsonFactory(),
                credential)
                .setApplicationName(calendar.getApplicationName())
                .build();
    }

    public List<Event> getEvents() throws IOException {
        Events events = calendar.events().list("primary").execute();
        return events.getItems();
    }

    public Event addEvent(Event event) throws IOException {
        return calendar.events().insert("primary", event).execute();
    }

    public void deleteEvent(String eventId) throws IOException {
        calendar.events().delete("primary", eventId).execute();
    }

    public Event updateEvent(String eventId, Event event) throws IOException {
        return calendar.events().update("primary", eventId, event).execute();
    }
}