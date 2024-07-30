package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.*;
import group8.skyweaver_inventory.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
//import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.MessagingException;


@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderedProductRepository orderedProductRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GmailService gmailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> register) {
        String username = register.get("username");
        String password = register.get("password");
        String accesslevel = register.get("accesslevel").toUpperCase();
        String gmail = register.get("gmail");

        // Validate access level
        if (!accesslevel.equals("MANAGER") && !accesslevel.equals("EMPLOYEE")) {
            return ResponseEntity.badRequest().body("Invalid access level.");
        }

        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }

        if (userRepository.findByGmail(gmail) != null) {
            return ResponseEntity.badRequest().body("Account with the same gmail already exists.");
        }

        // Validate password
        if (!isValidPassword(password)) {
            return ResponseEntity.badRequest().body("Password must be 8+ characters, and have at least 1 capital and number.");
        }

        // Save new user
        User user = new User(username, password, accesslevel);
        user.setGmail(gmail);
        if (accesslevel == "EMPLOYEE")
        {
            user.setIsAvailable(true);
        }

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully.");
    }

    private boolean isValidPassword(String password) {
        int minLength = 8;
        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);

        return password.length() >= minLength && hasNumber && hasUpperCase;
    }

    @GetMapping("/login")
    public String getLogin(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            if (user.getAccesslevel().equals("MANAGER")) {
                return "redirect:/manager/homepage.html";
            } else if (user.getAccesslevel().equals("EMPLOYEE")) {
                return "redirect:/employee/homepage.html";
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

            if (user.getToken() == null || user.getToken().isEmpty()) {
                return "redirect:/authorize";
            }

            if (accesslevel.equals("MANAGER")) {
                return "redirect:/manager/homepage.html";
            } else if (accesslevel.equals("EMPLOYEE")) {
                return "redirect:/employee/homepage.html";
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
    public RedirectView redirectToLogin(HttpSession session) {
        session.invalidate();
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
        if (user == null || user.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
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

        List<OrderedProduct> orders = orderedProductRepository.findByOrderByProductNameAsc();
        model.addAttribute("ordersCount", orders.size());
        model.addAttribute("o", orders);
        return "manager/homepage";
    }

    @GetMapping("/employee/homepage.html")
    public String employeeHomepage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getAccesslevel() == "MANAGER") {
            return "redirect:/";
        }
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

    @GetMapping("/manager/inbox")
    public String managerInbox(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || Objects.equals(user.getAccesslevel(), "EMPLOYEE")) {
            return "redirect:/";
        }

        User fullyLoadedUser = userService.getUserWithMessages(user.getUid());
        model.addAttribute("messages", fullyLoadedUser.getMessages());

        return "manager/managerinbox";
    }

    @GetMapping("/employee/inbox")
    public String employeeInbox(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || Objects.equals(user.getAccesslevel(), "MANAGER")) {
            return "redirect:/";
        }

        User fullyLoadedUser = userService.getUserWithMessages(user.getUid());
        model.addAttribute("messages", fullyLoadedUser.getMessages());

        return "employee/employeeinbox";
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable int id) {
        Optional<Message> message = messageRepository.findById(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<Map<String, String>> sendMessage(
            @RequestParam("recipient") String recipientUsername,
            @RequestParam("messageName") String messageName,
            @RequestParam("messageContent") String messageContent,
            HttpSession session) throws MessagingException, IOException, GeneralSecurityException {
        System.out.println("Received sendMessage request");

        User sender = (User) session.getAttribute("user");
        User recipient = userRepository.findByUsername(recipientUsername);


        Message message = new Message();
        message.setMessageName(messageName);
        message.setMessageContent(messageContent);
        message.setTimeSent(LocalDateTime.now().toString());
        message.setMessageSender(sender.getUsername());
        message.setUser(recipient);

        recipient.getMessages().add(message);
        userRepository.save(recipient);

        // Send email through Gmail API
        gmailService.sendEmail(sender.getGmail(), recipient.getGmail(), messageName, messageContent);

        // Determine redirect URL based on user access level
        String redirectUrl = "redirect:/";
        if ("MANAGER".equalsIgnoreCase(sender.getAccesslevel())) {
            redirectUrl = "/manager/homepage.html";
        } else if ("EMPLOYEE".equalsIgnoreCase(sender.getAccesslevel())) {
            redirectUrl = "/employee/homepage.html";
        }

        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl, "message", "Message sent successfully"));
    }

    // endpoint only for testing, since tests don't like gmail part
    @PostMapping("/sendTestMessage")
    public ResponseEntity<Map<String, String>> sendTestMessage(
            @RequestParam("recipient") String recipientUsername,
            @RequestParam("messageName") String messageName,
            @RequestParam("messageContent") String messageContent,
            HttpSession session) {

        System.out.println("Received sendTestMessage request");

        User sender = (User) session.getAttribute("user");
        User recipient = userRepository.findByUsername(recipientUsername);

        Message message = new Message();
        message.setMessageName(messageName);
        message.setMessageContent(messageContent);
        message.setTimeSent(LocalDateTime.now().toString());
        message.setMessageSender(sender.getUsername());
        message.setUser(recipient);

        recipient.getMessages().add(message);
        userRepository.save(recipient);

        // Skipping Gmail sending part
        // gmailService.sendEmail(sender.getGmail(), recipient.getGmail(), messageName, messageContent);

        // Determine redirect URL based on user access level
        String redirectUrl = "redirect:/";
        if ("MANAGER".equalsIgnoreCase(sender.getAccesslevel())) {
            redirectUrl = "/manager/homepage.html";
        } else if ("EMPLOYEE".equalsIgnoreCase(sender.getAccesslevel())) {
            redirectUrl = "/employee/homepage.html";
        }

        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl, "message", "Message sent successfully"));
    }

    @GetMapping("/messageform")
    public String showMessageForm(HttpSession session, Model model) {
        User manager = (User) session.getAttribute("user");
        if (manager == null) {
            return "redirect:/";
        }

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "personalized/messageform";
    }

    @GetMapping("/returnfrommessage")
    public RedirectView returnFromMessage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // Redirect to the login page if the user is not authenticated
            return new RedirectView("/");
        }
        if (user.getAccesslevel().equalsIgnoreCase("MANAGER")) {
            return new RedirectView("/manager/inbox");
        } else if (user.getAccesslevel().equalsIgnoreCase("EMPLOYEE")) {
            return new RedirectView("/employee/inbox");
        } else {
            return new RedirectView("/");
        }
    }

    @GetMapping("/manager/viewMyEmployees.html")
    public String viewMyEmployees(HttpSession session, Model model) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || manager.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        List<User> myEmployees = userRepository.findByManager(manager);
        model.addAttribute("employees", myEmployees);
        return "manager/viewMyEmployees";
    }

    @GetMapping("/manager/addMyEmployees.html")
    public String viewAvailableEmployees(HttpSession session, Model model) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        List<User> availableEmployees = userRepository.findByAccesslevelAndIsAvailable("EMPLOYEE", true);
        model.addAttribute("employees", availableEmployees);
        return "manager/addMyEmployees";
    }

    @PostMapping("/manager/addEmployee")
    public String addEmployeeToTeam(@RequestParam String username, HttpSession session) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || manager.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        User employee = userRepository.findByUsername(username);
        if (manager != null && employee != null) {
            employee.setManager(manager);
            employee.setIsAvailable(false);
            userRepository.save(employee);
        }
        return "redirect:/manager/addMyEmployees.html";
    }

    @PostMapping("/manager/removeEmployee")
    public String removeEmployeeFromTeam(HttpSession session, @RequestParam String username) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        User employee = userRepository.findByUsername(username);
        if (employee != null) {
            employee.setManager(null);
            employee.setIsAvailable(true);
            employee.setSalary(0);
            userRepository.save(employee);
        }
        return "redirect:/manager/viewMyEmployees.html";
    }

    @GetMapping("/employee/myManager.html")
    public String viewMyManager(HttpSession session, Model model) {
        User employee = (User) session.getAttribute("user");
        if (employee == null || employee.getAccesslevel() == "MANAGER") {
            return "redirect:/";
        }

        else if (employee != null) {
            User manager = employee.getManager();
            if (manager != null) {
                User managerDetails = userRepository.findById(manager.getUid());
                if (managerDetails != null) {
                    model.addAttribute("manager", managerDetails);
                } else {
                    model.addAttribute("noManager", true);
                }
            } else {
                model.addAttribute("noManager", true);
            }
        }
        return "employee/myManager";
    }

    @PostMapping("/manager/adjustSalary")
    public String adjustSalary(HttpSession session, @RequestParam String username, @RequestParam double salary, RedirectAttributes redirectAttributes) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }

        User employee = userRepository.findByUsername(username);
        if (salary < 17.4) {
            redirectAttributes.addFlashAttribute("error", "Minimum Wage $17.40/h, please input a valid value.");
        } else {
            employee.setSalary(salary);
            userRepository.save(employee);
            redirectAttributes.addFlashAttribute("success", "Salary adjusted successfully.");
        }
        return "redirect:/manager/viewMyEmployees.html";
    }

    @GetMapping("/employee/mySalary.html")
    public String viewMySalary(HttpSession session, Model model) {
        User employee = (User) session.getAttribute("user");
        if (employee == null || employee.getAccesslevel() == "MANAGER") {
            return "redirect:/";
        }

        if (employee.getSalary() == 0.0) {
            model.addAttribute("noSalary", true);
        } else {
            model.addAttribute("salary", "$" + employee.getSalary() + "/h");
        }

        return "employee/mySalary";
    }

    @GetMapping("/session/user")
    @ResponseBody
    public String getSessionUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user.getUsername();
        }
        return "";
    }

}
