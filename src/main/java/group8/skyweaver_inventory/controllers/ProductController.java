package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
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

    @PostMapping("/manager/add")
    public String addProduct(@RequestParam Map<String, String> newProduct, HttpServletResponse response)
    {
        String productName = newProduct.get("productName");
        int productQuantity = Integer.parseInt(newProduct.get("productQuantity"));
        float productPrice = Float.parseFloat(newProduct.get("productPrice"));
        String productCategory = newProduct.get("productCategory");
        productRepository.save(new Product(productName, productQuantity, productPrice, productCategory));
        response.setStatus(201);
        return "/productAdded.html";
    }    
    @GetMapping("/managestock")
    public String redirect() {
        return "manager/managestock.html";
    }

}
