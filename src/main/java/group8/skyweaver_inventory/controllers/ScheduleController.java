package group8.skyweaver_inventory.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import group8.skyweaver_inventory.models.Schedule;
import group8.skyweaver_inventory.models.ScheduleRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import jakarta.servlet.http.HttpSession;


@Controller
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/employee/schedule")
    public String getSchedule(HttpSession session, Model model) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        User user = (User) session.getAttribute("user"); //This line will likely cause issues while testing?*
        
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
    public String modifySchedule(@RequestParam String user,
                             @RequestParam(required = false) boolean oneOff,
                             @RequestParam int year,
                             @RequestParam int month,
                             @RequestParam int day,
                             @RequestParam String startTime,
                             @RequestParam double duration,
                             @RequestParam String description) {

        LocalTime localTime = LocalTime.parse(startTime);

        // Create LocalDateTime object
        LocalDateTime startDateTime = LocalDateTime.of(year, month, day, localTime.getHour(), localTime.getMinute());

        // Calculate endTime based on startTime and duration
        LocalDateTime endTime = startDateTime.plusHours((long) duration);

        // Determine weekly based on oneOff checkbox
        boolean weekly = true;
        if (oneOff) {
            weekly = false;
        }

        User currentuser = userRepository.findByUsername(user);
        // Create a Schedule object
        Schedule entry = new Schedule();
        entry.setWeekly(weekly);
        entry.setStartTime(startDateTime);
        entry.setEndTime(endTime);
        entry.setUser(currentuser);
        
        scheduleRepository.save(entry);

        // TODO: Connect this entry to Google Calendar API with description
        return "redirect:/manager/schedule?user=" + user;
    }

    @PostMapping("/manager/deleteschedule/{sid}")
    public String deleteFromSchedule(@PathVariable("sid") String sid) {
        Long id = Long.parseLong(sid);
        String user = scheduleRepository.getById(id).getUser().getUsername();
        // TODO: Remove entry from Google Calendar API
        scheduleRepository.deleteById(id);
        return "redirect:/manager/schedule?user=" + user;
    }
}