package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Schedule;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.ScheduleRepository;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.CalendarService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.springframework.mock.web.MockHttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class ScheduleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private ScheduleController scheduleController;

    @Mock
    private Model model;

    private MockHttpSession session;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
        session = new MockHttpSession();
    }

    // Test null session to employee/schedule endpoint
    @Test
    public void testGetSchedule_NullSessionUser() throws Exception {
        session.setAttribute("user", null);

        mockMvc.perform(get("/employee/schedule").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login.html"));
    }

    // Test session with accesslevel 'Employee' to employee/schedule endpoint
    @Test
    public void testGetSchedule_EmployeeUser() throws Exception {
        User employee = new User();
        employee.setAccesslevel("EMPLOYEE");
        session.setAttribute("user", employee);

        mockMvc.perform(get("/employee/schedule").session(session))
                .andExpect(view().name("employee/viewschedule"));
    }

    // Test null session to manager/schedule endpoint
    @Test
    public void testGetScheduleManager_NullSessionUser() throws Exception {
        session.setAttribute("user", null);

        mockMvc.perform(get("/manager/schedule").param("user", "user1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login.html"));
    }

    // Test session with accesslevel 'Employee' to manager/schedule endpoint
    @Test
    public void testGetScheduleManager_EmployeeUser() throws Exception {
        User employee = new User();
        employee.setAccesslevel("EMPLOYEE");
        session.setAttribute("user", employee);

        mockMvc.perform(get("/manager/schedule").param("user", "user1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login.html"));
    }

    // Test session with accesslevel 'Manager' to manager/schedule endpoint
    @Test
    public void testGetScheduleManager_ManagerUser() throws Exception {
        User manager = new User();
        manager.setAccesslevel("Manager");
        session.setAttribute("user", manager);
        when(scheduleRepository.findByUserUsername("user1")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/manager/schedule").param("user", "user1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/manageschedule"));
    }

    // Test manager/modifyschedule endpoint with successful data
    @Test
    public void testModifySchedule_Success() throws Exception {
        User manager = new User();
        manager.setAccesslevel("Manager");
        session.setAttribute("user", manager);
        when(userRepository.findByUsername("user1")).thenReturn(manager);

        mockMvc.perform(post("/manager/modifyschedule")
                .session(session)
                .param("user", "user1")
                .param("oneOff", "false")
                .param("year", "2024")
                .param("month", "7")
                .param("day", "13")
                .param("startTime", "10:00")
                .param("duration", "2")
                .param("description", "Meeting"))
                .andExpect(redirectedUrl("/manager/schedule?user=user1"));

        verify(scheduleRepository, times(1)).save(any(Schedule.class));
        verify(calendarService, times(1)).addEvent(any(), any());
    }

    // Test manager/deleteschedule with a failure to delete
    @Test
    public void testDeleteSchedule_Failure() throws Exception {
        User manager = new User();
        manager.setAccesslevel("Manager");
        session.setAttribute("user", manager);

        // Create a schedule with valid details
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setUser(manager);
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 13, 10, 0); // Example valid start time
        schedule.setStartTime(startTime);
        when(scheduleRepository.getById(1L)).thenReturn(schedule);

        // Mock calendarService to throw IOException when deleteEvent is called
        doThrow(new IOException("Failed to delete event")).when(calendarService).deleteEvent(anyString(), anyString());

        mockMvc.perform(post("/manager/deleteschedule/1").session(session))
                .andExpect(redirectedUrl("/manager/schedule?user=" + manager.getUsername()));

        // Verify that scheduleRepository.deleteById was never called
        verify(scheduleRepository, never()).deleteById(1L);
        
        // Verify that calendarService.deleteEvent was called exactly once
        verify(calendarService, never()).deleteEvent(eq(schedule.getUser().getGmail()), anyString());
    }

    // Test manager/deleteschedule with a successful delete
    @Test
    public void testDeleteSchedule_Success() throws Exception {
        // Create a manager user
        User manager = new User();
        manager.setAccesslevel("Manager");
        
        // Set up session attribute
        session.setAttribute("user", manager);
        
        // Mock schedule entry in the repository
        LocalDateTime startTime = LocalDateTime.of(2024, 7, 13, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 7, 13, 12, 0);
        Schedule schedule = new Schedule(1L, startTime, endTime, false, manager);
        when(scheduleRepository.getById(1L)).thenReturn(schedule);

        // Mock Google Calendar event
        Event googleEvent = new Event();
        googleEvent.setId("mockEventId");
        googleEvent.setSummary("Skyweaver Work Shift");
        googleEvent.setDescription("Meeting");
        googleEvent.setStart(new EventDateTime().setDateTime(new DateTime("2024-07-13T10:00:00-07:00")));
        googleEvent.setEnd(new EventDateTime().setDateTime(new DateTime("2024-07-13T12:00:00-07:00")));
        List<Event> mockEvents = Collections.singletonList(googleEvent);
        when(calendarService.getEvents(manager.getGmail())).thenReturn(mockEvents);

        // Perform deletion action
        mockMvc.perform(post("/manager/deleteschedule/1").session(session))
                .andExpect(redirectedUrl("/manager/schedule?user=" + manager.getUsername()));

        // Verify deletions
        verify(scheduleRepository, times(1)).deleteById(1L);
        verify(calendarService, times(1)).deleteEvent(manager.getGmail(), "mockEventId");
    }
}
