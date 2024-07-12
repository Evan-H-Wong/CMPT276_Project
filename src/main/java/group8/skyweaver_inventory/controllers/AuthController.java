package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.CalendarService;
import group8.skyweaver_inventory.CalendarConfig;
import com.google.api.client.auth.oauth2.Credential;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    private final CalendarConfig calendarConfig;
    private final CalendarService calendarService;
    private final UserRepository userRepository;

    public AuthController(CalendarConfig calendarConfig, CalendarService calendarService, UserRepository userRepository) {
        this.calendarConfig = calendarConfig;
        this.calendarService = calendarService;
        this.userRepository = userRepository;
    }

    @GetMapping("/authorize")
    public String authorize(HttpSession session) throws IOException, GeneralSecurityException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login.html";
        }
        String state = user.getGmail(); // Use Gmail as the state
        session.setAttribute("oauth2_state", state);
        String authorizationUrl = calendarConfig.getAuthorizationUrl(user.getGmail()); // Pass Gmail to getAuthorizationUrl
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam String code, @RequestParam String state, HttpSession session, Model model) {
        String storedState = (String) session.getAttribute("oauth2_state");
        session.removeAttribute("oauth2_state"); // Remove the state after one-time use

        try {
            Credential credential = calendarConfig.exchangeCode(code, storedState);
            calendarService.setCredential(storedState, credential); // Use user's email (Gmail) as the key

            User user = userRepository.findByGmail(storedState); // Use findByGmail to fetch user
            if (user != null) {
                user.setToken(credential.getAccessToken());

                // Save the refresh token
                String refreshToken = credential.getRefreshToken();
                if (refreshToken != null) {
                    user.setRefreshToken(refreshToken);
                }
                
                userRepository.save(user);
            }
            model.addAttribute("message", "Authorization successful!");
            model.addAttribute("access", user.getAccesslevel());
        } catch (Exception e) {
            model.addAttribute("message", "Authorization failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "manager/oauth2callback";
    }
}
