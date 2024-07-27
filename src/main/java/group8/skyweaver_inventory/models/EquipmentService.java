package group8.skyweaver_inventory.models;

import group8.skyweaver_inventory.models.Equipment;
import group8.skyweaver_inventory.models.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public Equipment getEquipmentById(int id) {
        return equipmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Equipment not found"));
    }

    public void saveOrUpdateEquipment(Equipment equipment) {
        equipmentRepository.save(equipment);
    }

    public void markAsDefective(int id) {
        Equipment equipment = getEquipmentById(id);
        equipment.setEquipmentStatus("Defective");
        equipmentRepository.save(equipment);
    }

    public void updateExpirationDate(int id, String expirationDate) {
        Equipment equipment = getEquipmentById(id);
        equipment.setExpirationDate(expirationDate);
        equipmentRepository.save(equipment);
    }

    public void deleteEquipment(int id) {
        equipmentRepository.deleteById(id);
    }
}
