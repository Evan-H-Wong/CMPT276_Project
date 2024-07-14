package group8.skyweaver_inventory.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import group8.skyweaver_inventory.models.Schedule;
import group8.skyweaver_inventory.models.ScheduleRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.CalendarService;
import jakarta.servlet.http.HttpSession;


@Controller
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarService calendarService;

    @GetMapping("/employee/schedule")
    public String getSchedule(HttpSession session, Model model) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login.html";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        //Schedule temp = new Schedule((long)1, LocalDateTime.now().plusHours(24), LocalDateTime.now().plusHours(32), true, user);
        //scheduleRepository.save(temp);

        List<Schedule> Shifts = scheduleRepository.findByUserUsername(user.getUsername());
        List<String> Date = new ArrayList<>();
        List<Integer> StartTimeHour = new ArrayList<>();
        List<Integer> StartTimeMinute = new ArrayList<>();
        List<Integer> EndTimeHour = new ArrayList<>();
        List<Integer> EndTimeMinute = new ArrayList<>();
        List<String> DayOfWeek = new ArrayList<>();
        List<Boolean> Weekly = new ArrayList<>();
        for (int i = 0; i < Shifts.size(); i++)
        {
            Date.add((Shifts.get(i)).getStartTime().format(formatter));
            StartTimeHour.add((Shifts.get(i)).getStartTime().getHour());
            StartTimeMinute.add((Shifts.get(i)).getStartTime().getMinute());
            EndTimeHour.add((Shifts.get(i)).getEndTime().getHour());
            EndTimeMinute.add((Shifts.get(i)).getEndTime().getMinute());
            DayOfWeek.add((Shifts.get(i)).getStartTime().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            Weekly.add((Shifts.get(i)).getWeekly());
        }
        model.addAttribute("date", Date);
        model.addAttribute("starthour", StartTimeHour);
        model.addAttribute("startminute", StartTimeMinute);
        model.addAttribute("endhour", EndTimeHour);
        model.addAttribute("endminute", EndTimeMinute);
        model.addAttribute("dayweek", DayOfWeek);
        model.addAttribute("weekly", Weekly);
        return "employee/viewschedule";
    }

    @GetMapping("/manager/schedule")
    public String getScheduleManager(@RequestParam String user, HttpSession session, Model model) throws IOException {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/auth/login.html";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        //Schedule temp = new Schedule((long)1, LocalDateTime.now().plusHours(24), LocalDateTime.now().plusHours(32), true, user);
        //scheduleRepository.save(temp);

        List<Schedule> Shifts = scheduleRepository.findByUserUsername(user);
        List<String> Date = new ArrayList<>();
        List<Integer> StartTimeHour = new ArrayList<>();
        List<Integer> StartTimeMinute = new ArrayList<>();
        List<Integer> EndTimeHour = new ArrayList<>();
        List<Integer> EndTimeMinute = new ArrayList<>();
        List<String> DayOfWeek = new ArrayList<>();
        List<Boolean> Weekly = new ArrayList<>();
        List<Long> IDList = new ArrayList<>();
        for (int i = 0; i < Shifts.size(); i++)
        {
            Date.add((Shifts.get(i)).getStartTime().format(formatter));
            StartTimeHour.add((Shifts.get(i)).getStartTime().getHour());
            StartTimeMinute.add((Shifts.get(i)).getStartTime().getMinute());
            EndTimeHour.add((Shifts.get(i)).getEndTime().getHour());
            EndTimeMinute.add((Shifts.get(i)).getEndTime().getMinute());
            DayOfWeek.add((Shifts.get(i)).getStartTime().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            Weekly.add((Shifts.get(i)).getWeekly());
            IDList.add(Shifts.get(i).getId());
        }
        model.addAttribute("date", Date);
        model.addAttribute("starthour", StartTimeHour);
        model.addAttribute("startminute", StartTimeMinute);
        model.addAttribute("endhour", EndTimeHour);
        model.addAttribute("endminute", EndTimeMinute);
        model.addAttribute("dayweek", DayOfWeek);
        model.addAttribute("weekly", Weekly);
        model.addAttribute("username", user);
        model.addAttribute("id", IDList);
        return "manager/manageschedule";
    }

    @PostMapping("/manager/modifyschedule")
    public String modifySchedule(HttpSession session,
                                @RequestParam String user,
                                @RequestParam(required = false) boolean oneOff,
                                @RequestParam int year,
                                @RequestParam int month,
                                @RequestParam int day,
                                @RequestParam String startTime,
                                @RequestParam double duration,
                                @RequestParam String description) throws GeneralSecurityException {

        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/auth/login.html";
        }

        // Define the timezone
        ZoneId zoneId = ZoneId.of("Canada/Pacific");

        // Parse start time
        LocalTime localTime = LocalTime.parse(startTime);

        // Create ZonedDateTime object
        ZonedDateTime startDateTime = ZonedDateTime.of(year, month, day, localTime.getHour(), localTime.getMinute(), 0, 0, zoneId);

        // Calculate endTime based on startTime and duration
        ZonedDateTime endDateTime = startDateTime.plusHours((long) duration);

        // Determine weekly based on oneOff checkbox
        boolean weekly = !oneOff;

        User currentUser = userRepository.findByUsername(user);

        // Create a Schedule object
        Schedule entry = new Schedule();
        entry.setWeekly(weekly);
        entry.setStartTime(startDateTime.toLocalDateTime());
        entry.setEndTime(endDateTime.toLocalDateTime());
        entry.setUser(currentUser);

        // Add event to Google Calendar
        try {
            // Construct Event object
            Event event = new Event()
                    .setSummary("Skyweaver Work Shift") // Use description as event summary
                    .setLocation("") // Set location if needed
                    .setDescription(description); // Description of the event

            // Set start and end times
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
            String startStr = startDateTime.format(formatter);
            String endStr = endDateTime.format(formatter);
            EventDateTime start = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(startStr))
                    .setTimeZone("Canada/Pacific");
            EventDateTime end = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(endStr))
                    .setTimeZone("Canada/Pacific");
            event.setStart(start);
            event.setEnd(end);

            if (weekly) {
                // Add recurrence rule for weekly events
                String recurrenceRule = "RRULE:FREQ=WEEKLY";
                event.setRecurrence(List.of(recurrenceRule));
            }

            // Call CalendarService to add the event
            calendarService.addEvent(currentUser.getGmail(), event);
            // Save the schedule entry to the database
            scheduleRepository.save(entry);
        } catch (IOException e) {
            // Handle API call errors
            e.printStackTrace();
            System.out.println("failed to add to calendar");
        }

        // Redirect to manage schedule page
        return "redirect:/manager/schedule?user=" + user;
    }


    @PostMapping("/manager/deleteschedule/{sid}")
    public String deleteFromSchedule(HttpSession session, @PathVariable("sid") String sid) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/auth/login.html";
        }

        Long id = Long.parseLong(sid);
        Schedule schedule = scheduleRepository.getById(id);
        String user = schedule.getUser().getUsername();

        // Retrieve start time from schedule
        LocalDateTime startTime = schedule.getStartTime();
        //boolean isWeekly = schedule.getWeekly();

        try {
            // Get all events from user's Google Calendar
            List<Event> events = calendarService.getEvents(schedule.getUser().getGmail());

            // Define a DateTimeFormatter for parsing and formatting
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

            for (Event event : events) {
                // Check if the event summary matches
                if ("Skyweaver Work Shift".equals(event.getSummary())) {
                    ZonedDateTime eventStartTime = ZonedDateTime.parse(event.getStart().getDateTime().toStringRfc3339(), formatter);
                    if (eventStartTime.toLocalTime().equals(startTime.toLocalTime()) &&
                        eventStartTime.getDayOfWeek().equals(startTime.getDayOfWeek())) {
                        // Delete the entire recurring event series
                        calendarService.deleteEvent(schedule.getUser().getGmail(), event.getId());

                        // Delete schedule entry from local database
                        scheduleRepository.deleteById(id);
                        break; // Exit loop after deleting
                    }
                }
            }

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace(); // Handle exception appropriately
        }

        return "redirect:/manager/schedule?user=" + user;
    }
}