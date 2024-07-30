package group8.skyweaver_inventory.models;

import group8.skyweaver_inventory.models.Equipment;
import group8.skyweaver_inventory.models.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public void updateEquipmentStatus() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate today = LocalDate.now();

        // Fetch all equipment
        List<Equipment> equipmentList = equipmentRepository.findAll();

        for (Equipment equipment : equipmentList) {
            // Check if equipment is not defective and if its expiration date is passed
            if (!equipment.getEquipmentStatus().equals("Defective")) {
                LocalDate expirationDate;
                try {
                    expirationDate = LocalDate.parse(equipment.getExpirationDate(), formatter);
                } catch (Exception e) {
                    continue; // Skip if the expiration date is invalid
                }

                if (today.isAfter(expirationDate)) {
                    equipment.setEquipmentStatus("Expired");
                    equipmentRepository.save(equipment);
                } else {
                    // If expiration date is updated to a future date, reset status to "Available"
                    if (equipment.getEquipmentStatus().equals("Expired")) {
                        equipment.setEquipmentStatus("Available");
                        equipmentRepository.save(equipment);
                    }
                }
            }
        }
    }
}
