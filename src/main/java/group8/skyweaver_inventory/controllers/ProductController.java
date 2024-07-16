package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
import group8.skyweaver_inventory.models.OrderedProduct;
import group8.skyweaver_inventory.models.OrderedProductRepository;
import group8.skyweaver_inventory.models.User;
//import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

//import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.view.RedirectView;

import java.util.stream.Collectors;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;



@Controller
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderedProductRepository orderedProductRepository;
    @PostMapping("manager/productAdded")
    public String addProduct(@RequestParam Map<String, String> newProduct, HttpSession session, HttpServletResponse response, Model model)
    {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }

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
    public String productAddedRedirect(HttpSession session, Model model) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        model.addAttribute("p",products);
        return "manager/productAdded.html";
    }

    @PostMapping("manager/products/delete/{pid}")
    public String deleteProduct(HttpSession session, @PathVariable ("pid") int id) 
    {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        productRepository.deleteById(id);
        return "redirect:/manager/managestock";
    }
    
    @GetMapping("manager/order")
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
    public String newOrder(@RequestParam Map<String, String> newOrder, HttpServletResponse response, Model model) throws ParseException {
        int orderQuantity = Integer.parseInt(newOrder.get("orderQuantity"));
        String productName = newOrder.get("productName");
        String productCategory = newOrder.get("productCategory");
        int productQuantity = Integer.parseInt(newOrder.get("productQuantity"));
        float productPrice = Float.parseFloat(newOrder.get("productPrice"));
        if (orderedProductRepository.findByProductName(productName) != null) {
            return "redirect:/auth/orderError.html";
        }
        
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int randomDay = (int) (Math.random() * (31 - day) + day);
        int randomHour = (int) (Math.random() * (24 - hour) + hour);
        int randomMinute = (int) (Math.random() * (60 - minute) + minute);

        calendar.set(Calendar.DAY_OF_MONTH, randomDay);
        calendar.set(Calendar.HOUR_OF_DAY, randomHour);
        calendar.set(Calendar.MINUTE, randomMinute);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Date arrivalDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String formattedDate = sdf.format(arrivalDate);
        Date randomDate = sdf.parse(formattedDate);
        
        orderedProductRepository.save(new OrderedProduct(productName, productQuantity, productPrice, productCategory, orderQuantity, randomDate));
        response.setStatus(201);
        List<OrderedProduct> orderedProducts = orderedProductRepository.findByOrderByProductNameAsc();
        model.addAttribute("o", orderedProducts);
        return "manager/orderAdded";
    }
    
    @GetMapping("manager/orderAdded")
    public String orderAddedRedirect(Model model) {
        List<OrderedProduct> orders = orderedProductRepository.findByOrderByProductNameAsc();
        model.addAttribute("o", orders);
        return "manager/orderAdded.html";
    }

    @PostMapping("/order/delete/{pid}")
    public String deleteOrder(HttpSession session, @PathVariable ("pid") int id) 
    {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        orderedProductRepository.deleteById(id);
        return "redirect:/manager/order";
    }



    

    @GetMapping("manager/managestock")
    public String stockRedirect(HttpSession session, Model model) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj->obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj->obj.getProductQuantity() < 12).collect(Collectors.toList());
        model.addAttribute("p", products);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        return "manager/managestock.html";
    }

    @GetMapping("/employee/viewstock")
    public String stockView(HttpSession session, Model model) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null) {
            return "redirect:/";
        }
        List<Product> products = productRepository.findByOrderByProductNameAsc();
        List<Product> outofstock = products.stream().filter(obj->obj.getProductQuantity() == 0).collect(Collectors.toList());
        List<Product> lowstock = products.stream().filter(obj->obj.getProductQuantity() < 12).collect(Collectors.toList());
        model.addAttribute("p", products);
        model.addAttribute("rowCount", products.size());
        model.addAttribute("outofstock", outofstock.size());
        model.addAttribute("lowstock", lowstock.size());
        return "employee/viewstock.html";
    }

    @GetMapping("manager/editproduct")
    public String editStock(HttpSession session, Model model, @RequestParam Map<String, String> ToEdit) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
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

    @PostMapping("manager/applyproduct")
    public String applyStock(HttpSession session, @RequestParam Map<String, String> ToApply) {
        User usercheck = (User) session.getAttribute("user");
        if (usercheck == null || usercheck.getAccesslevel() == "EMPLOYEE") {
            return "redirect:/";
        }
        System.out.println(ToApply.get("name"));
        Product modProduct = productRepository.findById(Integer.parseInt(ToApply.get("pid"))).get();
        modProduct.setProductName(ToApply.get("name"));
        modProduct.setProductCategory(ToApply.get("category"));
        String temp = String.format("%.2f", Float.parseFloat(ToApply.get("price")));
        modProduct.setProductPrice(Float.parseFloat(temp));
        modProduct.setProductQuantity(Integer.parseInt(ToApply.get("quantity")));
        productRepository.save(modProduct);
        return "redirect:/manager/managestock";
    }

}
