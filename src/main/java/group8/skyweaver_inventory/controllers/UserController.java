package group8.skyweaver_inventory.controllers;


import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/auth/login.html";
    }

    @GetMapping("/register")
    public String getRegister() {
        return "redirect:/auth/register.html";
    }

    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> register, HttpServletResponse response) {
        String username = register.get("username");
        String password = register.get("password");
        String accesslevel = register.get("accesslevel");

        // makes sure to make any manager or employee all uppercase characters
        accesslevel = accesslevel.toUpperCase();

        // if it is not Manager, manager, Employee, or employee, it will return an error and go back to the register page
        if (!accesslevel.equals("MANAGER") && !accesslevel.equals("EMPLOYEE")) {
            response.setStatus(400);
            return "redirect:/auth/register.html";
        }

        System.out.println("Username: " + username);
        // Encode the password before saving
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword, accesslevel);
        userRepository.save(user);
        response.setStatus(201);
        return "redirect:/auth/login.html";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/auth/login.html";
    }

    @GetMapping("/default")
    public String defaultAfterLogin(HttpServletRequest request) {
        if (request.isUserInRole("MANAGER")) {
            return "redirect:/personalized/manager.html";
        }
        return "redirect:/personalized/employee.html";
    }
}