package group8.skyweaver_inventory.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByUsernameAndPassword(String username, String password);
    List<User> findByAccesslevelAndIsAvailable(String accesslevel, boolean isAvailable);
    List<User> findByManager(User manager);
} 
