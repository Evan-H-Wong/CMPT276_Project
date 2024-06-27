package group8.skyweaver_inventory.controllers;



import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String redirect() {
        return "/login.html";
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String, String> login, Model model, HttpServletRequest request, HttpSession session) {
        String username = login.get("username");
        String password = login.get("password");
        String accesslevel = login.get("role");

        List<User> users = userRepository.findByUsernameAndPasswordAndAccesslevel(username, password, accesslevel);
        System.out.println(accesslevel);
        // now there is either 1 or 0 users in the list, 1 if there is already a registered user with that username and password
        if(users.isEmpty()) {
            return "login.html";
        } else {
            User user = users.get(0);
            request.getSession().setAttribute("sessionUser", user);
            model.addAttribute("user", user);
            return "protected.html";
        }
    }

    @GetMapping("/login")
    public String getLogin(Model model, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("sessionUser");
        if (user != null) {
            return "/login.html";
        } else {
            model.addAttribute("user", user);
            return "/protected.html";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> register, HttpServletResponse response) {
        String username = register.get("username");
        String password = register.get("password");
        String accesslevel = register.get("accesslevel");
        User user = new User(username, password, accesslevel);
        userRepository.save(user);
        response.setStatus(201);
        return "/login.html";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "/login.html";
    }

}
