package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
import group8.skyweaver_inventory.models.OrderedProduct;
import group8.skyweaver_inventory.models.OrderedProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.stream.Collectors;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;



@Controller
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderedProductRepository orderedProductRepository;
    @PostMapping("/productAdded")
    public String addProduct(@RequestParam Map<String, String> newProduct, HttpServletResponse response, Model model)
    {
        String productName = newProduct.get("productName");
        int productQuantity = Integer.parseInt(newProduct.get("productQuantity"));
        float productPrice = Float.parseFloat(newProduct.get("productPrice"));
        String productCategory = newProduct.get("productCategory");
        if (productRepository.findByProductName(productName) != null) {
            return "redirect:/auth/productError.html";
        }

        productRepository.save(new Product(productName, productQuantity, productPrice, productCategory));
        response.setStatus(201);
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        model.addAttribute("p", products);
        return "manager/productAdded";
    }   
    
// This page can only be accessed via adding a new product, if you refresh the page,
// it resubmits the form and duplicates the item you added, and writing manager/productAdded
// in the url will break the css (the header, aside, footer)
    @GetMapping("manager/productAdded")
    public String productAddedRedirect(Model model) {
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        model.addAttribute("p",products);
        return "manager/productAdded.html";
    }

    @PostMapping("/products/delete/{pid}")
    public String deleteProduct(@PathVariable ("pid") int id) 
    {
        productRepository.deleteById(id);
        return "redirect:/managestock";
    }
    
    @GetMapping("/manager/order")
    public String orderRedirect(Model model) {
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<OrderedProduct> orderedProducts = orderedProductRepository.findByOrderByProductNameAsc();
        model.addAttribute("o", orderedProducts);
        model.addAttribute("p", products);
        
        return "manager/order.html";
    }

    @GetMapping("/manager/order/{pid}")
    public String productOrderRedirect(@PathVariable ("pid") int id, Model model) 
    {
        Product product = productRepository.findByPid(id);
        model.addAttribute("o", product);
        return "manager/confirmOrder.html";
    }

    @PostMapping("/orderConfirmed")
    public String newOrder(@RequestParam Map<String, String> newOrder, Model model) throws ParseException {
        int orderQuantity = Integer.parseInt(newOrder.get("orderQuantity"));
        String productName = newOrder.get("productName");
        String productCategory = newOrder.get("productCategory");
        int productQuantity = Integer.parseInt(newOrder.get("productQuantity"));
        float productPrice = Float.parseFloat(newOrder.get("productPrice"));
        
        String randomDate = newOrder.get("arrivalDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date arrivalDate = sdf.parse(randomDate);
        
        
        orderedProductRepository.save(new OrderedProduct(productName, productQuantity, productPrice, productCategory, orderQuantity, arrivalDate));
        List<OrderedProduct> orderedProducts = orderedProductRepository.findByOrderByProductNameAsc();
        model.addAttribute("o", orderedProducts);
        return "redirect:/manager/order";
    }
    
    @GetMapping("/product/delete/{pid}")
    public String deleteOrder(@PathVariable ("pid") int id) 
    {
        orderedProductRepository.deleteById(id);
        return "redirect:/manager/order";
    }



    

    @GetMapping("/managestock")
    public String stockRedirect(Model model) {
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj->obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj->obj.getProductQuantity() < 12).collect(Collectors.toList());
        model.addAttribute("p", products);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        return "manager/managestock.html";
    }

    @GetMapping("/viewstock")
    public String stockView(Model model) {
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj->obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj->obj.getProductQuantity() < 12).collect(Collectors.toList());
        model.addAttribute("p", products);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        return "employee/viewstock.html";
    }

    @GetMapping("/editproduct")
    public String editStock(Model model, @RequestParam Map<String, String> ToEdit) {
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj->obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj->obj.getProductQuantity() < 12).collect(Collectors.toList());
        Product product = productRepository.findByPid(Integer.parseInt(ToEdit.get("toEdit")));
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        model.addAttribute("p", product);
        return "manager/editproduct.html";
    }

    @PostMapping("/applyproduct")
    public String applyStock(@RequestParam Map<String, String> ToApply) {
        System.out.println(ToApply.get("name"));
        Product modProduct = productRepository.findById(Integer.parseInt(ToApply.get("pid"))).get();
        modProduct.setProductName(ToApply.get("name"));
        modProduct.setProductCategory(ToApply.get("category"));
        String temp = String.format("%.2f", Float.parseFloat(ToApply.get("price")));
        modProduct.setProductPrice(Float.parseFloat(temp));
        modProduct.setProductQuantity(Integer.parseInt(ToApply.get("quantity")));
        productRepository.save(modProduct);
        return "redirect:/managestock";
    }

}
