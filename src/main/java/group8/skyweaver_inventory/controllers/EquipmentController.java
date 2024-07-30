package group8.skyweaver_inventory.controllers;
import group8.skyweaver_inventory.models.Equipment;
import group8.skyweaver_inventory.models.EquipmentService;
import group8.skyweaver_inventory.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    public String listEquipment(Model model) {
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        model.addAttribute("equipments", equipmentList);
        return "personalized/equipment";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable("id") int id) {
        try {
            Equipment equipment = equipmentService.getEquipmentById(id);
            return ResponseEntity.ok(equipment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/add")
    public String addEquipment(@ModelAttribute Equipment equipment) {
        equipmentService.saveOrUpdateEquipment(equipment);
        return "redirect:/equipment";
    }

    @PostMapping("/mark-defective/{id}")
    public ResponseEntity<Void> markAsDefective(@PathVariable("id") int id) {
        try {
            equipmentService.markAsDefective(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/update-expiration/{id}")
    public ResponseEntity<Void> updateExpirationDate(@PathVariable("id") int id, @RequestParam("expirationDate") String expirationDate) {
        try {
            Equipment equipment = equipmentService.getEquipmentById(id);
            equipment.setExpirationDate(expirationDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate newExpirationDate = LocalDate.parse(expirationDate, formatter);
            LocalDate today = LocalDate.now();

            if (today.isAfter(newExpirationDate)) {
                equipment.setEquipmentStatus("Expired");
            } else {
                equipment.setEquipmentStatus("Available");
            }

            equipmentService.saveOrUpdateEquipment(equipment);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/remove/{id}")
    public ResponseEntity<Void> removeEquipment(@PathVariable("id") int id) {
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/update-status")
    public ResponseEntity<Void> updateEquipmentStatus() {
        try {
            List<Equipment> equipmentList = equipmentService.getAllEquipment();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate today = LocalDate.now();

            for (Equipment equipment : equipmentList) {
                LocalDate expirationDate = LocalDate.parse(equipment.getExpirationDate(), formatter);
                if (today.isAfter(expirationDate)) {
                    equipment.setEquipmentStatus("Expired");
                } else if (equipment.getEquipmentStatus().equals("Expired")) {
                    equipment.setEquipmentStatus("Available");
                }
                equipmentService.saveOrUpdateEquipment(equipment);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/personalized/equipment")
    public String personalizedEquipment(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        // Fetch equipment data and add to the model
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        model.addAttribute("equipments", equipmentList);

        return "personalized/equipment";
    }

    @GetMapping("/returnfromequipment")
    public String personalizedEquipmentReturn(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        if (user.getAccesslevel().equals("MANAGER")) {
            return "redirect:/manager/homepage.html";
        } else {
            return "redirect:/employee/homepage.html";
        }
    }

    @GetMapping("/equipmentform")
    public String showEquipmentForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        return "personalized/equipmentform";
    }
}
