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

import com.fasterxml.jackson.databind.ObjectMapper;

//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
//import java.util.Optional;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterSuccess() throws Exception {
        String username = "Fred";
        String password = "Password1";
        String accesslevel = "MANAGER";
        String gmail = "gmail@gmail.com";

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password,
                        "accesslevel", accesslevel,
                        "gmail", gmail
                ))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("User registered successfully."));
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
        user.setToken("test_token");
    
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
                .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html?error=true"));
    }

    @Test
    public void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(MockMvcRequestBuilders.get("/logout").session(session))
               .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
               .andExpect(MockMvcResultMatchers.redirectedUrl("/auth/login.html"));
    }

     @Test
    public void testViewMyEmployees() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User manager = new User("fred", "password", "MANAGER");
        session.setAttribute("user", manager);

        List<User> myEmployees = new ArrayList<>();
        myEmployees.add(new User("sam", "password", "EMPLOYEE"));

        when(userRepository.findByManager(manager)).thenReturn(myEmployees);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/viewMyEmployees.html")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("employees", myEmployees))
                .andExpect(MockMvcResultMatchers.view().name("manager/viewMyEmployees"));
    }

    @Test
    public void testViewAvailableEmployees() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User manager = new User("fred", "password", "MANAGER");
        session.setAttribute("user", manager);

        List<User> availableEmployees = new ArrayList<>();
        availableEmployees.add(new User("sam", "password", "EMPLOYEE"));

        when(userRepository.findByAccesslevelAndIsAvailable("EMPLOYEE", true)).thenReturn(availableEmployees);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/addMyEmployees.html")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("employees", availableEmployees))
                .andExpect(MockMvcResultMatchers.view().name("manager/addMyEmployees"));
    }

    @Test
    public void testAddEmployeeToTeam() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User manager = new User("fred", "password", "MANAGER");
        session.setAttribute("user", manager);

        User employee = new User("sam", "password", "EMPLOYEE");
        employee.setIsAvailable(true);

        when(userRepository.findByUsername("sam")).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/addEmployee")
                .session(session)
                .param("username", "sam"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/addMyEmployees.html"));
    }

    @Test
    public void testRemoveEmployeeFromTeam() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User manager = new User("fred", "password", "MANAGER");
        session.setAttribute("user", manager);

        User employee = new User("sam", "password", "EMPLOYEE");

        when(userRepository.findByUsername("sam")).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/removeEmployee")
                .session(session)
                .param("username", "sam"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/viewMyEmployees.html"));
    }

    @Test
    public void testViewMyManagerWithoutManager() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User employee = new User("sam", "password", "EMPLOYEE");
        session.setAttribute("user", employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/employee/myManager.html")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("noManager", true))
                .andExpect(MockMvcResultMatchers.view().name("employee/myManager"));
    }

    @Test
    public void testAdjustSalarySuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User managerUser = new User("fred", "password", "MANAGER");
        session.setAttribute("user", managerUser);

        String username = "sam";
        double salary = 20.0;
        User employee = new User(username, "password", "EMPLOYEE");
        when(userRepository.findByUsername(username)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/adjustSalary")
                .session(session)
                .param("username", username)
                .param("salary", String.valueOf(salary)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attribute("success", "Salary adjusted successfully."))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/viewMyEmployees.html"));
    }

    @Test
    public void testAdjustSalaryFailureDueToLowSalary() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User managerUser = new User("fred", "password", "MANAGER");
        session.setAttribute("user", managerUser);

        String username = "sam";
        double salary = 15.0;
        User employee = new User(username, "password", "EMPLOYEE");
        when(userRepository.findByUsername(username)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/adjustSalary")
                .session(session)
                .param("username", username)
                .param("salary", String.valueOf(salary)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.flash().attribute("error", "Minimum Wage $17.40/h, please input a valid value."))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/viewMyEmployees.html"));
    }

    @Test
    public void testAdjustSalaryRedirectForEmployee() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User employeeUser = new User("sam", "password", "EMPLOYEE");
        session.setAttribute("user", employeeUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/adjustSalary")
                .session(session)
                .param("username", "sam")
                .param("salary", "20.0"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"));
    }

    @Test
    public void testViewMySalaryWithSalary() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User employeeUser = new User("sam", "password", "EMPLOYEE");
        employeeUser.setSalary(20.0);
        session.setAttribute("user", employeeUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/employee/mySalary.html")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("salary", "$20.0/h"))
                .andExpect(MockMvcResultMatchers.view().name("employee/mySalary"));
    }

    @Test
    public void testViewMySalaryWithNoSalary() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User employeeUser = new User("sam", "password", "EMPLOYEE");
        employeeUser.setSalary(0.0);
        session.setAttribute("user", employeeUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/employee/mySalary.html")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("noSalary", true))
                .andExpect(MockMvcResultMatchers.view().name("employee/mySalary"));
    }
}
