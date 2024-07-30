package group8.skyweaver_inventory.models;

//import group8.skyweaver_inventory.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    Message findByMid(int mid);
}