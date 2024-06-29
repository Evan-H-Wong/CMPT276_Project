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
    public String redirectToLogin() {
        return "redirect:/authentication/login.html";
    }

    @GetMapping("/register")
    public String getRegister() {
        return "redirect:/authentication/register.html";
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String, String> login, Model model) {
        int uid = Integer.parseInt(login.get("uid"));
        String username = login.get("username");
        String password = login.get("password");
        String accesslevel = login.get("accesslevel");

        User user = userRepository.findById(uid).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            if (accesslevel.equalsIgnoreCase("manager")) {
                return "redirect:/manager.html";
            } else if (accesslevel.equalsIgnoreCase("employee")) {
                return "redirect:/employee.html";
            } else {
                return "redirect:/authentication/error.html";
            }
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "/authentication/login.html";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> register, HttpServletResponse response) {
        String username = register.get("username");
        String password = register.get("password");
        String accesslevel = register.get("accesslevel");

        // if it is not Manager, manager, Employee, or employee, it will return an error and go back to the register page
        if (!accesslevel.equalsIgnoreCase("Manager") && !accesslevel.equalsIgnoreCase("Employee")) {
            response.setStatus(400);
            return "redirect:/authentication/register.html";
        }

        // makes sure to make any manager or employee all uppercase characters
        if (accesslevel.equalsIgnoreCase("manager")) {
            accesslevel = "MANAGER";
        } else if (accesslevel.equalsIgnoreCase("employee")) {
            accesslevel = "EMPLOYEE";
        }

        User user = new User(username, password, accesslevel);
        userRepository.save(user);
        response.setStatus(201);

        if (accesslevel.equalsIgnoreCase("manager")) {
            return "redirect:/manager.html";
        } else if (accesslevel.equalsIgnoreCase("employee")) {
            return "redirect:/employee.html";
        } else {
            return "redirect:/authentication/error.html";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "/authentication/login.html";
    }
}
