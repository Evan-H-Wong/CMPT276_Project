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
        return "manager/productAdded.html";
    }    

    @PostMapping("/products/delete/{pid}")
    public String deleteRectangle(@PathVariable ("pid") int id) 
    {
        productRepository.deleteById(id);
        return "redirect:/managestock";
    }
    
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

    @PostMapping("/applyproduct")
    public String applyStock(@RequestParam Map<String, String> ToApply) {
        System.out.println(ToApply.get("name"));
        Product modProduct = productRepository.findById(Integer.parseInt(ToApply.get("pid"))).get();
        modProduct.setProductName(ToApply.get("name"));
        modProduct.setProductCategory(ToApply.get("category"));
        modProduct.setProductPrice(Float.parseFloat(ToApply.get("price")));
        modProduct.setProductQuantity(Integer.parseInt(ToApply.get("quantity")));
        productRepository.save(modProduct);
        return "redirect:/managestock";
    }

}
