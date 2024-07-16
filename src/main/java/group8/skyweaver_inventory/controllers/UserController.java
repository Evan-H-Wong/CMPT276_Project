package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;



import jakarta.servlet.http.HttpSession;

//import java.util.ArrayList;
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
        String gmail = username;

        // Validate access level
        if (!accesslevel.equals("MANAGER") && !accesslevel.equals("EMPLOYEE")) {
            return "redirect:/auth/error.html";
        }

        if (userRepository.findByUsername(username) != null || userRepository.findByGmail(gmail) != null) {
            return "redirect:/auth/error.html";
        }

        // Save new user
        User user = new User(username, password, accesslevel);
        user.setGmail(gmail);
        if (accesslevel == "EMPLOYEE")
        {
            user.setIsAvailable(true);
        }

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

            if (user.getToken() == null || user.getToken().isEmpty()) {
                return "redirect:/authorize";
            }

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
        return "manager/homepage";
    }

    @GetMapping("/employee/homepage.html")
    public String employeeHomepage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getAccesslevel() == "MANAGER") {
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
        return "employee/homepage";
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
                User managerDetails = userRepository.findById(manager.getUid()).orElse(null);
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
