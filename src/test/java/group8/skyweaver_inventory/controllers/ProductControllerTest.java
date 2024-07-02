package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.t;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Test
    public void testAddProduct() throws Exception {
        Map<String, String> newProduct = new HashMap<>();
        newProduct.put("productName", "Test Product");
        newProduct.put("productQuantity", "10");
        newProduct.put("productPrice", "19.99");
        newProduct.put("productCategory", "TestCategory");

        mockMvc.perform(MockMvcRequestBuilders.post("/productAdded")
                .param("productName", "Test Product")
                .param("productQuantity", "10")
                .param("productPrice", "19.99")
                .param("productCategory", "TestCategory")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.view().name("manager/productAdded"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p"));
    }

    @Test
    public void testProductAddedRedirect() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Product1", 10, 15.99f, "Category1"));
        products.add(new Product("Product2", 5, 25.99f, "Category2"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/productAdded"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("manager/productAdded.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p"));
    }

    @Test
    public void testDeleteProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/products/delete/1"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/managestock"));
    }

    @Test
    public void testStockRedirect() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Product1", 10, 15.99f, "Category1"));
        products.add(new Product("Product2", 5, 25.99f, "Category2"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/managestock"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("manager/managestock.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }

    @Test
    public void testStockView() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Product1", 10, 15.99f, "Category1"));
        products.add(new Product("Product2", 5, 25.99f, "Category2"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/viewstock"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("employee/viewstock.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }

    @Test
    public void testEditStock() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Product1", 10, 15.99f, "Category1"));
        products.add(new Product("Product2", 5, 25.99f, "Category2"));

        Product product = new Product("Product1", 0, 15.99f, "Category1");
        product.setPid(1);

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);
        when(productRepository.findByPid(1)).thenReturn(product);

        mockMvc.perform(MockMvcRequestBuilders.get("/editproduct")
                .param("toEdit", "1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("manager/editproduct.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }

    @Test
    public void testApplyStock() throws Exception {
        Product product = new Product("Product1", 0, 15.99f, "Category1");
        product.setPid(1);

        when(productRepository.findById(1)).thenReturn(java.util.Optional.of(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/applyproduct")
                .param("pid", "1")
                .param("name", "Updated Product")
                .param("category", "UpdatedCategory")
                .param("price", "20.00")
                .param("quantity", "5"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/managestock"));
    }
}
