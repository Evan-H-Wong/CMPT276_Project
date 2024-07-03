package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;

import org.junit.jupiter.api.Test;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRegisterSuccess() throws Exception {
        String username = "Fred";
        String password = "password";
        String accesslevel = "MANAGER";

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .param("username", username)
                .param("password", password)
                .param("accesslevel", accesslevel))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())  
                .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html"));
    }

    @Test
    public void testRedirectToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html"));
    }

    @Test
    public void testGetLoginRedirectWithoutSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html"));
    }

    @Test
    public void testGetLoginRedirectWithManagerSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User managerUser = new User("Fred", "password", "MANAGER");
        session.setAttribute("user", managerUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/login").session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/personalized/manager.html"));
    }

    @Test
    public void testGetLoginRedirectWithEmployeeSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User employeeUser = new User("Bob", "password", "EMPLOYEE");
        session.setAttribute("user", employeeUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/login").session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/personalized/employee.html"));
    }

    @Test
    public void testPostLoginSuccess() throws Exception {
        String username = "Fred";
        String password = "password";
        String accesslevel = "MANAGER";
    
        User user = new User(username, password, accesslevel);
    
        when(userRepository.findByUsernameAndPassword(username, password)).thenReturn(user);
    
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password)
                .param("accesslevel", accesslevel))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("user", Matchers.is(user)))
                .andExpect(MockMvcResultMatchers.view().name("personalized/manager"));
    }

    @Test
    public void testPostLoginFailure() throws Exception {
        String username = "Fred";
        String password = "password";
        String accesslevel = "MANAGER";

        when(userRepository.findByUsernameAndPassword(username, password)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password)
                .param("accesslevel", accesslevel))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html"));
    }

    @Test
    public void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(MockMvcRequestBuilders.get("/logout").session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html"));
    }
}