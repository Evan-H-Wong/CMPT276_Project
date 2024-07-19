package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.CalendarConfig;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.api.client.auth.oauth2.Credential;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarConfig calendarConfig;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private UserRepository userRepository;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    // Test null user to authorize endpoint
    @Test
    void testAuthorize_NullSessionUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/authorize").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"));
    }

    // Test successful redirect to authentication url
    @Test
    void testAuthorize_ValidSessionUser() throws Exception {
        User mockUser = new User();
        mockUser.setGmail("test@gmail.com");
        session.setAttribute("user", mockUser);
        when(calendarConfig.getAuthorizationUrl(anyString())).thenReturn("http://auth-url");

        mockMvc.perform(MockMvcRequestBuilders.get("/authorize").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://auth-url"));

        assert "test@gmail.com".equals(session.getAttribute("oauth2_state"));
    }

    // Test failed try-catch block during authentication
    @Test
    void testOauth2Callback_ExceptionInTryCatch() throws Exception {
        // Create a new mock user with access level "Manager"
        User mockUser = new User();
        mockUser.setGmail("test@gmail.com");
        mockUser.setAccesslevel("MANAGER");
        session.setAttribute("user", mockUser);

        // Set up session attribute and mock behavior
        session.setAttribute("oauth2_state", "test@gmail.com");
        doThrow(new IOException("Test Exception")).when(calendarConfig).exchangeCode(anyString(), anyString());

        // Perform the callback request and verify the response
        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/callback")
                        .session(session)
                        .param("code", "code")
                        .param("state", "state"))
                .andExpect(MockMvcResultMatchers.view().name("manager/oauth2callback"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "Authorization failed: Test Exception"));
                
        assertNull(mockUser.getToken(), "Token should be null");
        assertNull(mockUser.getRefreshToken(), "Refresh token should be null");
    }

    // Test successful authentication after try-catch block
    @Test
    void testOauth2Callback_SuccessfulAuthorization() throws Exception {
        session.setAttribute("oauth2_state", "test@gmail.com");
        
        // Create and configure the mock user
        User mockUser = new User();
        mockUser.setGmail("test@gmail.com");
        mockUser.setUsername("test");
        mockUser.setAccesslevel("MANAGER");
        session.setAttribute("user", mockUser);

        // Set up mocks
        when(userRepository.findByGmail(anyString())).thenReturn(mockUser);
        Credential mockCredential = mock(Credential.class);
        when(mockCredential.getAccessToken()).thenReturn("access_token");
        when(mockCredential.getRefreshToken()).thenReturn("refresh_token");
        when(calendarConfig.exchangeCode(anyString(), anyString())).thenReturn(mockCredential);

        // Perform the request and verify the results
        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/callback")
                        .session(session)
                        .param("code", "code")
                        .param("state", "state"))
                .andExpect(MockMvcResultMatchers.view().name("manager/oauth2callback"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "Authorization successful!"));

        // Verify the save operation was called on the user repository
        verify(userRepository).save(mockUser);

        // Verify that the mock user's token and refresh token are set correctly
        assertEquals("access_token", mockUser.getToken());
        assertEquals("refresh_token", mockUser.getRefreshToken());
    }
}
