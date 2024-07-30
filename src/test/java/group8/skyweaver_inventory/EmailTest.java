package group8.skyweaver_inventory;

import group8.skyweaver_inventory.models.Message;
import group8.skyweaver_inventory.models.MessageRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.GmailService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class EmailTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MessageRepository messageRepository;

    @MockBean
    private GmailService gmailService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSendTestEmail() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        User bob = new User("Bob", "password", "EMPLOYEE");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        when(userRepository.findByUsername("Bob")).thenReturn(bob);

        mockMvc.perform(MockMvcRequestBuilders.post("/sendTestMessage")
                        .session(session)
                        .param("recipient", "Bob")
                        .param("messageName", "Schedule Change")
                        .param("messageContent", "Your schedule has been changed"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Message message = new Message("Schedule Change", "Your schedule has been changed", "now", "Fred", bob);

        List<Message> bobMessages = new ArrayList<>();
        bobMessages.add(message);
        bob.setMessages(bobMessages);

        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + message.getId())
                        .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageContent", Matchers.is("Your schedule has been changed")));
    }


    @Test
    public void testManagerInbox() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        List<Message> messages = new ArrayList<>();
        Message message1 = new Message("Meeting", "There is a meeting tomorrow", "yesterday", "Alice", fred);
        messages.add(message1);

        fred.setMessages(messages);
        when(userRepository.findByUsername("Fred")).thenReturn(fred);

        when(messageRepository.findById(1)).thenReturn(Optional.of(message1));

        mockMvc.perform(MockMvcRequestBuilders.get("/messages/1").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageContent", Matchers.is("There is a meeting tomorrow")));
    }

    @Test
    public void testNewEmployeeWelcomeEmail() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User bob = new User("Bob", "password", "EMPLOYEE");
        session.setAttribute("user", bob);

        Message welcomeMessage = new Message("Welcome", "Welcome to the company!", "now", "Company", bob);
        List<Message> messages = Collections.singletonList(welcomeMessage);
        bob.setMessages(messages);
        when(userRepository.findByUsername("Bob")).thenReturn(bob);

        when(messageRepository.findById(0)).thenReturn(Optional.of(welcomeMessage));

        mockMvc.perform(MockMvcRequestBuilders.get("/messages/0").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageContent", Matchers.is("Welcome to the company!")));
    }

    @Test
    public void testViewEmail() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User bob = new User("Bob", "password", "EMPLOYEE");
        session.setAttribute("user", bob);

        Message message = new Message("Meeting", "There is a meeting tomorrow", "yesterday", "Alice", bob);

        when(messageRepository.findById(1)).thenReturn(Optional.of(message));

        mockMvc.perform(MockMvcRequestBuilders.get("/messages/1").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageContent", Matchers.is("There is a meeting tomorrow")));
    }

    @Test
    public void testManagerGmailService() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User george = new User("George", "password", "MANAGER");
        george.setGmail("george@gmail.com");
        session.setAttribute("user", george);

        User mike = new User("Mike", "password", "EMPLOYEE");
        mike.setGmail("mike@gmail.com");
        when(userRepository.findByUsername("George")).thenReturn(george);
        when(userRepository.findByUsername("Mike")).thenReturn(mike);

        doNothing().when(gmailService).sendEmail(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/sendMessage")
                .param("recipient", "Mike")
                .param("messageName", "Next Shift")
                .param("messageContent", "Review items and check for consistency")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.redirectUrl").value("/manager/homepage.html"));

        verify(gmailService, times(1)).sendEmail(
                eq("george@gmail.com"),
                eq("mike@gmail.com"),
                eq("Next Shift"),
                eq("Review items and check for consistency")
        );
    }
    
    @Test
    public void testEmployeeGmailService() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User daniel = new User("Daniel", "password", "EMPLOYEE");
        daniel.setGmail("daniel@gmail.com");
        session.setAttribute("user", daniel);

        User jeff = new User("Jeff", "password", "MANAGER");
        jeff.setGmail("jeff@gmail.com");
        when(userRepository.findByUsername("Daniel")).thenReturn(daniel);
        when(userRepository.findByUsername("Jeff")).thenReturn(jeff);

        doNothing().when(gmailService).sendEmail(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/sendMessage")
                .param("recipient", "Jeff")
                .param("messageName", "New Shift")
                .param("messageContent", "Can my shift be pushed back by an hour for the following year?")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.redirectUrl").value("/employee/homepage.html"));

        verify(gmailService, times(1)).sendEmail(
                eq("daniel@gmail.com"),
                eq("jeff@gmail.com"),
                eq("New Shift"),
                eq("Can my shift be pushed back by an hour for the following year?")
        );
    }
}
