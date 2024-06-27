package group8.skyweaver_inventory.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByNameAndCategory(String productName, String productCategory);
    List<Product> findByPid(Long pid);
}
