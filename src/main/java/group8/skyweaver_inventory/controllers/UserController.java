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
    public String login(@RequestParam Map<String, String> login, Model model, HttpServletRequest request, HttpSession session) {
        int uid = Integer.parseInt(login.get("uid"));
        String username = login.get("username");
        String password = login.get("password");
        String accesslevel = login.get("accesslevel");

        User user = UserRepository.findById(uid).orElse(null);
        System.out.println(accesslevel);

        if (user != null && user.getUsername().equals(username) && user.getPassword().equals(password) && user.getAccesslevel().equals(accesslevel)) {
            session.setAttribute("sessionUser", user);
            model.addAttribute("user", user);
            if(user.getAccesslevel().equals("Manager") || user.getAccesslevel().equals("manager")){
                return "redirect:/manager.html";
            } else if(user.getAccesslevel().equals("Employee") || user.getAccesslevel().equals("employee")){
                return "redirect:/employee.html";
            } else {
                return "redirect:/authentication/error.html";
            }
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "/authentication/login.html";
        }
    }

    @GetMapping("/login")
    public String getLogin(Model model, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("sessionUser");
        if (user != null) {
            return "/authentication/login.html";
        } else {
            model.addAttribute("user", user);
            if(user.getAccesslevel().equals("Manager") || user.getAccesslevel().equals("manager")){
                return "redirect:/manager.html";
            } else if(user.getAccesslevel().equals("Employee") || user.getAccesslevel().equals("employee")){
                return "redirect:/employee.html";
            } else {
                return "redirect:/authentication/error.html";
            }
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

        if(accesslevel.equals("Manager") || accesslevel.equals("manager")){
            return "redirect:/manager.html";
        } else if(accesslevel.equals("Employee") || accesslevel.equals("employee")){
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
