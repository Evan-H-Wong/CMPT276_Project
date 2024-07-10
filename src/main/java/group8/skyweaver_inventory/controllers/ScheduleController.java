package group8.skyweaver_inventory.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import group8.skyweaver_inventory.models.Schedule;
import group8.skyweaver_inventory.models.ScheduleRepository;
import group8.skyweaver_inventory.models.User;
//import group8.skyweaver_inventory.models.UserRepository;
import jakarta.servlet.http.HttpSession;


@Controller
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/employee/schedule")
    public String getSchedule(HttpSession session, Model model) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        User user = (User) session.getAttribute("user"); //This line will likely cause issues while testing?*
        
        //
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

}