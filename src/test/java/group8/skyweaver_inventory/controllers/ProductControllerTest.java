package group8.skyweaver_inventory.controllers;

import group8.skyweaver_inventory.models.Product;
import group8.skyweaver_inventory.models.ProductRepository;
import group8.skyweaver_inventory.models.OrderedProduct;
import group8.skyweaver_inventory.models.OrderedProductRepository;
import group8.skyweaver_inventory.models.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private OrderedProductRepository orderedProductRepository;

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
    }

    //Test successful item addition
    @Test
    public void testProductAddSuccess() throws Exception {
        User user = new User();
        user.setAccesslevel("MANAGER");
        session.setAttribute("user", user);
        String pName = "Bananas";
        Integer pQuantity = 5;
        float pPrice = 3.70f;
        String pCategory = "Fruits";

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/productAdded")
                .param("productName", pName)
                .param("productQuantity", Integer.toString(pQuantity))
                .param("productPrice", Float.toString(pPrice))
                .param("productCategory", pCategory).session(session))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.view().name("manager/productAdded"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p"));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    //Test duplicate item addition
    @Test
    public void testProductAddFailure() throws Exception {
        User user = new User();
        user.setAccesslevel("MANAGER");
        session.setAttribute("user", user);
        Product existingProduct = new Product("Apples", 10, 3.00f, "Fruits");
        when(productRepository.findByProductName("Apples")).thenReturn(existingProduct);

        String pName = "Apples"; //Name already exists in database
        Integer pQuantity = 5;
        float pPrice = 3.70f;
        String pCategory = "Fruits";

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/productAdded")
                .param("productName", pName)
                .param("productQuantity", Integer.toString(pQuantity))
                .param("productPrice", Float.toString(pPrice))
                .param("productCategory", pCategory).session(session))
                .andExpect(MockMvcResultMatchers.status().isFound());
                verify(productRepository, times(0)).save(any(Product.class));
    }

    //Test successful item deletion
    @Test
    public void testProductRemoveSuccess() throws Exception {
        User user = new User();
        user.setAccesslevel("MANAGER");
        session.setAttribute("user", user);
        doNothing().when(productRepository).deleteById(1);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/products/delete/{pid}", 1)
                        .session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/managestock"));

        verify(productRepository, times(1)).deleteById(1);
    }

    //Test successful item edit
    @Test
    public void testProductEditSuccess() throws Exception {
        User user = new User();
        user.setAccesslevel("MANAGER");
        session.setAttribute("user", user);
        Product existingProduct = new Product("Apples", 20, 1.99f, "Fruits");
        when(productRepository.findById(anyInt())).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        mockMvc.perform(MockMvcRequestBuilders.post("/manager/applyproduct")
                        .param("pid", "49")
                        .param("name", "Apples")
                        .param("category", "Fruits")
                        .param("price", "2.99")
                        .param("quantity", "20").session(session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/managestock"));

        verify(productRepository, times(1)).findById(49);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    //Test redirect to managestock page
    @Test
    public void testManageStockRedirect() throws Exception {
        User user = new User();
        user.setAccesslevel("MANAGER");
        session.setAttribute("user", user);
        List<Product> products = new ArrayList<>();
        products.add(new Product("Apples", 20, 2.99f, "Fruits"));
        products.add(new Product("Bananas", 30, 3.99f, "Fruits"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/manager/managestock").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("manager/managestock.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }

    //Test redirect to viewstock page
    @Test
    public void testStockView() throws Exception {
        User user = new User();
        user.setAccesslevel("EMPLOYEE");
        session.setAttribute("user", user);
        List<Product> products = new ArrayList<>();
        products.add(new Product("Apples", 20, 2.99f, "Fruits"));
        products.add(new Product("Bananas", 30, 3.99f, "Fruits"));

        when(productRepository.findByOrderByProductNameAsc()).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/employee/viewstock").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("employee/viewstock.html"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("p", "rowCount", "outofstock", "lowstock"));
    }

    //Testing creating new orders
    @Test
    public void testOrderAddSuccess() throws Exception {
        String pName = "Watermelons";
        Integer pQuantity = 5;
        float pPrice = 3.70f;
        String pCategory = "Fruits";
        Integer oQuantity = 10;

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

        System.out.println("pName: " + pName);
        System.out.println("pQuantity: " + pQuantity);
        System.out.println("pPrice: " + pPrice);
        System.out.println("pCategory: " + pCategory);
        System.out.println("oQuantity: " + oQuantity);
        

        mockMvc.perform(MockMvcRequestBuilders.post("/orderConfirmed")
                .param("productName", pName)
                .param("productQuantity", Integer.toString(pQuantity))
                .param("productPrice", Float.toString(pPrice))
                .param("productCategory", pCategory)
                .param("orderQuantity", Integer.toString(oQuantity))
                .param("arrivalDate", formattedDate))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.view().name("manager/orderAdded"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("o"));
                
        verify(orderedProductRepository, times(1)).save(any(OrderedProduct.class));
    }

    //Testing duplicate orders
    @Test
    public void testOrderAddFailure() throws Exception {
        OrderedProduct existingOrder = new OrderedProduct("Watermelons", 10, 3.00f, "Fruits", 20);
        when(orderedProductRepository.findByProductName("Watermelons")).thenReturn(existingOrder);
 
        String pName = "Watermelons";
        Integer pQuantity = 20;
        float pPrice = 2.99f;
        String pCategory = "Fruits";
        Integer oQuantity = 11;

        mockMvc.perform(MockMvcRequestBuilders.post("/orderConfirmed")
                .param("productName", pName)
                .param("productQuantity", Integer.toString(pQuantity))
                .param("productPrice", Float.toString(pPrice))
                .param("productCategory", pCategory)
                .param("orderQuantity", Integer.toString(oQuantity)))
                .andExpect(MockMvcResultMatchers.status().isFound());
                verify(orderedProductRepository, times(0)).save(any(OrderedProduct.class));
    }
    //Test successful item deletion
    @Test
    public void testOrderSuccessfulDeletion() throws Exception {
        doNothing().when(orderedProductRepository).deleteById(1);

        mockMvc.perform(MockMvcRequestBuilders.post("/order/delete/{pid}", 1))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/manager/order"));

        verify(orderedProductRepository, times(1)).deleteById(1);
    }
}