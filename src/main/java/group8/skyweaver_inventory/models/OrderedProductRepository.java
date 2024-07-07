package group8.skyweaver_inventory.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderedProductRepository extends JpaRepository<OrderedProduct, Integer> {
    List<OrderedProduct> findByProductNameAndProductCategory(String productName, String productCategory);
    OrderedProduct findByPid(Integer pid);
    List<OrderedProduct> findByOrderByProductNameAsc();
    OrderedProduct findByProductName(String productName);
}
