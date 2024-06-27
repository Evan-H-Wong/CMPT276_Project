package group8.skyweaver_inventory.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByUsernameAndPasswordAndAccesslevel(String username, String password, String accesslevel);
}
