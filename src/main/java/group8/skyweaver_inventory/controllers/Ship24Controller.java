package group8.skyweaver_inventory.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import group8.skyweaver_inventory.services.Ship24Service;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;

@Controller
@RequestMapping("/api/ship24")
public class Ship24Controller {

    @Autowired
    private Ship24Service ship24Service;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/track")
    public String trackShipment(@RequestParam String trackingNumber, HttpSession session, Model model) {
    
        Map<String, Object> results = ship24Service.getTrackerResultsByTrackingNumber(trackingNumber);
        if (results == null) {
            results = new HashMap<>();
    }
        // results.forEach((key, value) -> {
        //     System.out.println("Key: " + key + "\t Value: " + value);
        // });
    
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login.html";
        }
        
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj -> obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj -> obj.getProductQuantity() < 12).collect(Collectors.toList());
        lowstock.sort(Comparator.comparingInt(Product::getProductQuantity));

        model.addAttribute("results", results);
        model.addAttribute("lowstockproducts", lowstock);
        model.addAttribute("outofstockproducts", outofstock);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        model.addAttribute("restock", lowstock);
        model.addAttribute("username", user.getUsername());

        model.addAttribute("results", results);
        return "manager/homepage";
    }

    // @GetMapping("/trackers/{trackerId}/results")
    // public Map<String, Object> getTrackerResultsById(@PathVariable String trackerId) {
    //     return ship24Service.getTrackerResultsById(trackerId);
    // }

    // @GetMapping("/trackers/search/{trackingNumber}/results")
    // public Map<String, Object> getTrackerResultsByTrackingNumber(@PathVariable String trackingNumber) {
    //     return ship24Service.getTrackerResultsByTrackingNumber(trackingNumber);
    // }
}
