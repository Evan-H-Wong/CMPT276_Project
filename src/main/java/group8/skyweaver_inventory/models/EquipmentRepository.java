package group8.skyweaver_inventory.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
}
