package group8.skyweaver_inventory.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByProductNameAndProductCategory(String productName, String productCategory);
    Product findByPid(Integer pid);
    List<Product> findByOrderByProductNameAsc();
}
