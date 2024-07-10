package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> register) {
        String username = register.get("username");
        String password = register.get("password");
        String accesslevel = register.get("accesslevel").toUpperCase();

        // Validate access level
        if (!accesslevel.equals("MANAGER") && !accesslevel.equals("EMPLOYEE")) {
            return "redirect:/auth/error.html";
        }

        if (userRepository.findByUsername(username) != null) {
            return "redirect:/auth/error.html";
        }
        // Save new user
        User user = new User(username, password, accesslevel);
        userRepository.save(user);

        return "redirect:/auth/login.html";
    }

    @GetMapping("/login")
    public String getLogin(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            if (user.getAccesslevel().equals("MANAGER")) {
                return "redirect:/personalized/manager.html";
            } else if (user.getAccesslevel().equals("EMPLOYEE")) {
                return "redirect:/personalized/employee.html";
            }
        }

        return "redirect:/auth/login.html";
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String, String> login, HttpSession session, Model model) {
        String username = login.get("username");
        String password = login.get("password");
        String accesslevel = login.get("accesslevel").toUpperCase();

        User user = userRepository.findByUsernameAndPassword(username, password);

        if (user != null && user.getAccesslevel().equals(accesslevel)) {
            session.setAttribute("user", user);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            if (accesslevel.equals("MANAGER")) {
                return "personalized/manager";
            } else if (accesslevel.equals("EMPLOYEE")) {
                return "personalized/employee";
            }
        }

        return "redirect:/auth/login.html?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login.html";
    }

    @GetMapping("/")
    public RedirectView redirectToLogin() {
        return new RedirectView("/auth/login.html");
    }

    @GetMapping("/auth/all.html")
    public String allUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "all";
    }

    @GetMapping("/manager/homepage.html")
    public String managerHomepage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj->obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj->obj.getProductQuantity() < 12).collect(Collectors.toList());
        lowstock.sort(Comparator.comparingInt(Product::getProductQuantity));
        model.addAttribute("lowstockproducts", lowstock);
        model.addAttribute("outofstockproducts", outofstock);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        model.addAttribute("restock", lowstock);
        model.addAttribute("username", user.getUsername());
        return "manager/homepage";
    }

    @GetMapping("/employee/homepage.html")
    public String employeeHomepage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj -> obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj -> obj.getProductQuantity() < 12).collect(Collectors.toList());
        lowstock.sort(Comparator.comparingInt(Product::getProductQuantity));
        model.addAttribute("lowstockproducts", lowstock);
        model.addAttribute("outofstockproducts", outofstock);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        model.addAttribute("restock", lowstock);
        model.addAttribute("username", user.getUsername());
        return "employee/homepage";
    }

    @GetMapping("/personalized/manager")
    public String personalizedManager(HttpSession session, Model model) {
        return "/personalized/manager";
    }

    @GetMapping("/manager/managerinbox")
    public String managerInbox(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "manager/managerinbox";
    }

    @GetMapping("/employee/inbox")
    public String employeeInbox(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "employee/employeeinbox";
    }

    @PostMapping("/sendMessage")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> messageInfo, HttpSession session) {
        String recipientUsername = messageInfo.get("recipient");
        String messageName = messageInfo.get("messageName");
        String messageContent = messageInfo.get("messageContent");

        User sender = (User) session.getAttribute("user");
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        User recipient = userRepository.findByUsername(recipientUsername);
        if (recipient == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recipient not found");
        }

        Message message = new Message();
        message.setMessageName(messageName);
        message.setMessageContent(messageContent);
        message.setTimeSent(LocalDateTime.now().toString());
        message.setMessageSender(sender.getUsername());
        message.setUser(recipient);

        recipient.getMessages().add(message);
        userRepository.save(recipient);

        return ResponseEntity.ok("Message sent successfully");
    }

    @GetMapping("/messageform")
    public String showMessageForm(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "personalized/messageform";
    }
}
