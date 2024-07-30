package group8.skyweaver_inventory;

import group8.skyweaver_inventory.models.Message;
import group8.skyweaver_inventory.models.MessageRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class EmailTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MessageRepository messageRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSendEmail() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        User bob = new User("Bob", "password", "EMPLOYEE");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        when(userRepository.findByUsername("Bob")).thenReturn(bob);

        // Perform the POST request to send the email
        mockMvc.perform(MockMvcRequestBuilders.post("/sendMessage")
                        .session(session)
                        .param("recipient", "Bob")
                        .param("messageName", "Schedule Change")
                        .param("messageContent", "Your schedule has been changed"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Create the message and check the contents
        Message message = new Message("Schedule Change", "Your schedule has been changed", "now", "Fred", bob);

        // Ensure message is properly associated
        List<Message> bobMessages = new ArrayList<>();
        bobMessages.add(message);
        bob.setMessages(bobMessages);

        // Mock the repository calls
        when(messageRepository.findById(1)).thenReturn(Optional.of(message));

        // Perform a GET request to retrieve the message and verify its content
        mockMvc.perform(MockMvcRequestBuilders.get("/messages/1").session(session))
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
}
