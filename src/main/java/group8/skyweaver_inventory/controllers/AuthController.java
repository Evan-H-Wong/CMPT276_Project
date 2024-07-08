package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.services.CalendarService;
import group8.skyweaver_inventory.CalendarConfig;

import com.google.api.client.auth.oauth2.Credential;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {
    private final CalendarConfig CalendarConfig;
    private final CalendarService CalendarService;

    public AuthController(CalendarConfig CalendarConfig, CalendarService CalendarService) {
        this.CalendarConfig = CalendarConfig;
        this.CalendarService = CalendarService;
    }

    @GetMapping("/authorize")
    public String authorize() {
        String authorizationUrl = CalendarConfig.getAuthorizationUrl();
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam String code, Model model) {
        try {
            Credential credential = CalendarConfig.exchangeCode(code);
            CalendarService.setCredential(credential);
            model.addAttribute("message", "Authorization successful!");
        } catch (Exception e) {
            model.addAttribute("message", "Authorization failed!");
        }
        return "manager/oauth2callback";
    }
}
