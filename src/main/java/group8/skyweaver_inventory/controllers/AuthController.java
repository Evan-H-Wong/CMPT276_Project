package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.CalendarService;
import group8.skyweaver_inventory.services.GmailService;
import group8.skyweaver_inventory.CalendarConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Profile;

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
    private final GmailService gmailService;
    private final UserRepository userRepository;

    public AuthController(CalendarConfig calendarConfig, CalendarService calendarService, GmailService gmailService, UserRepository userRepository) {
        this.calendarConfig = calendarConfig;
        this.calendarService = calendarService;
        this.gmailService = gmailService;
        this.userRepository = userRepository;
    }

    @GetMapping("/authorize")
    public String authorize(HttpSession session) throws IOException, GeneralSecurityException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        String state = user.getGmail(); // Use Gmail as the state
        session.setAttribute("oauth2_state", state);
        String authorizationUrl = calendarConfig.getAuthorizationUrl(user.getGmail()); // Pass Gmail to getAuthorizationUrl
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam String code, @RequestParam String state, HttpSession session, Model model) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null) {
            return "redirect:/";
        }

        String storedState = (String) session.getAttribute("oauth2_state");
        session.removeAttribute("oauth2_state"); // Remove the state after one-time use

        try {
            Credential credential = calendarConfig.exchangeCode(code, storedState);
            
            // Use the credential to get the Gmail service
            Gmail gmailServiceInstance = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("CMPT276-Project")
                    .build();
            
            // Get the user's profile from Gmail
            Profile profile = gmailServiceInstance.users().getProfile("me").execute();
            String authenticatedEmail = profile.getEmailAddress();

            // Verify the email matches
            if (!authenticatedEmail.equals(usercheck.getGmail())) {
                model.addAttribute("message", "Authorization failed: Email mismatch.");
                return "manager/oauth2callback";
            }

            calendarService.setCredential(authenticatedEmail, credential); // Use user's email (Gmail) as the key
            gmailService.setCredential(authenticatedEmail, credential); // Set Gmail credentials

            User user = userRepository.findByGmail(authenticatedEmail); // Use findByGmail to fetch user
            if (user != null) {
                user.setToken(credential.getAccessToken());

                // Save the refresh token
                String refreshToken = credential.getRefreshToken();
                if (refreshToken != null) {
                    user.setRefreshToken(refreshToken);
                }

                userRepository.save(user);
                model.addAttribute("message", "Authorization successful!");
                model.addAttribute("access", user.getAccesslevel());
            } else {
                model.addAttribute("message", "Authorization failed: User not found.");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Authorization failed: " + e.getMessage());
            e.printStackTrace();
        }
        return "manager/oauth2callback";
    }
}
