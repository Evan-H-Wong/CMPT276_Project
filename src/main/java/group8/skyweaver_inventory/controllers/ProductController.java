package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
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
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/managestock")
    public String stockRedirect(Model model) {
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        model.addAttribute("p",products);
        return "manager/managestock.html";
    }

    @GetMapping("/editproduct")
    public String editStock(Model model, @RequestParam Map<String, String> ToEdit) {
        Product product = productRepository.findByPid(Integer.parseInt(ToEdit.get("toEdit")));
        model.addAttribute("p", product);
        return "manager/editproduct.html";
    }

}
