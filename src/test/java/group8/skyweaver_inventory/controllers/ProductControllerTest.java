package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    //Test successful item addition
    @Test
    public void testProductAddSuccess() throws Exception {
        String pName = "Unique Corn Flakes Name";
        Integer pQuantity = 5;
        float pPrice = 3.70f;
        String pCategory = "Grocery";

        mockMvc.perform(MockMvcRequestBuilders.post("/productAdded")
                .param("productName", pName)
                .param("productQuantity", Integer.toString(pQuantity))
                .param("productPrice", Float.toString(pPrice))
                .param("productCategory", pCategory))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.view().name("manager/productAdded"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p"));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    //Test duplicate item addition
    @Test
    public void testProductAddFailure() throws Exception {
        String pName = "Apples"; //Name already exists in database
        Integer pQuantity = 5;
        float pPrice = 3.70f;
        String pCategory = "Grocery";

        mockMvc.perform(MockMvcRequestBuilders.post("/productAdded")
                .param("productName", pName)
                .param("productQuantity", Integer.toString(pQuantity))
                .param("productPrice", Float.toString(pPrice))
                .param("productCategory", pCategory))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
        verify(productRepository, times(0));
    }

    //Test successful item deletion
    @Test
    public void testProductRemoveSuccess() throws Exception {
        doNothing().when(productRepository).deleteById(49);

        mockMvc.perform(MockMvcRequestBuilders.post("/products/delete/{pid}", 49))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/managestock"));

        verify(productRepository, times(1)).deleteById(49);
    }

    //Test successful item edit
    @Test
    public void testProductEditSuccess() throws Exception {
        Product existingProduct = new Product("Apples", 20, 2.99f, "Fruits");
        when(productRepository.findById(anyInt())).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        mockMvc.perform(MockMvcRequestBuilders.post("/applyproduct")
                        .param("pid", "49")
                        .param("name", "Apples")
                        .param("category", "Fruits")
                        .param("price", "200.0")
                        .param("quantity", "20"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/managestock"));

        verify(productRepository, times(1)).findById(49);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    //Test redirect to managestock page
    @Test
    public void testManageStockRedirect() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Apples", 20, 2.99f, "Fruits"));
        products.add(new Product("Bananas", 30, 3.99f, "Fruits"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/managestock"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("manager/managestock.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }

    //Test redirect to viewstock page
    @Test
    public void testStockView() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Apples", 20, 2.99f, "Fruits"));
        products.add(new Product("Bananas", 30, 3.99f, "Fruits"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/viewstock"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("employee/viewstock.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }
}