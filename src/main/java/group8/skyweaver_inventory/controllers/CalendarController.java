package group8.skyweaver_inventory.controllers;


import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import group8.skyweaver_inventory.services.CalendarService;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
public class CalendarController {
    private final CalendarService CalendarService;

    public CalendarController(CalendarService CalendarService) {
        this.CalendarService = CalendarService;
    }

    @GetMapping("/schedules")
    public String getEvents(Model model) throws IOException {
        List<Event> events = CalendarService.getEvents();
        model.addAttribute("events", events);
        return "manager/schedules";
    }

    @PostMapping("/schedules")
    public String addEvent(
            @RequestParam String summary,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime
    ) throws IOException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        Event event = new Event()
                .setSummary(summary)
                .setLocation(location)
                .setDescription(description);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(dateFormat.parse(startDateTime)))
                .setTimeZone("Canada/Pacific");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(dateFormat.parse(endDateTime)))
                .setTimeZone("Canada/Pacific");
        event.setEnd(end);

        CalendarService.addEvent(event);
        return "redirect:/schedules";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable String id) throws IOException {
        CalendarService.deleteEvent(id);
        return "redirect:/events";
    }

    @PostMapping("/events/update/{id}")
    public String updateEvent(@PathVariable String id, @ModelAttribute Event event) throws IOException {
        CalendarService.updateEvent(id, event);
        return "redirect:/events";
    }
}