package group8.skyweaver_inventory.controllers;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import static org.mockito.Mockito.when;
// import org.hamcrest.Matchers;

// import static org.hamcrest.Matchers.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLogin() throws Exception {
        User user = new User("test", "test", "manager");
        List<User> users = new ArrayList<>();
        users.add(user);
        when(userRepository.findByUsername("test")).thenReturn(users);

        Map<String, String> login = new HashMap<>();
        login.put("username", "test");
        login.put("password", "test");
        login.put("accesslevel", "manager");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(APPLICATION_JSON_UTF8)
                .content(login.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("user", user))
                .andExpect(MockMvcResultMatchers.view().name("redirect:/manager.html"));
    }
}
