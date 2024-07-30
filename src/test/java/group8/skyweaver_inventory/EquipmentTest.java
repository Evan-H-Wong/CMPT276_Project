package group8.skyweaver_inventory;

import group8.skyweaver_inventory.models.Equipment;
import group8.skyweaver_inventory.models.EquipmentRepository;
import group8.skyweaver_inventory.models.User;
import group8.skyweaver_inventory.models.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipmentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testViewEquipmentPage() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        Equipment equipment = new Equipment("Chair", "Office Chair", "Available", "01/01/2025");
        equipmentRepository.save(equipment);

        mockMvc.perform(MockMvcRequestBuilders.get("/equipment").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Chair")));
    }

    @Test
    public void testAddEquipment() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        mockMvc.perform(MockMvcRequestBuilders.post("/equipment/add")
                        .session(session)
                        .param("equipmentName", "Desk")
                        .param("equipmentDescription", "Office Desk")
                        .param("equipmentStatus", "Available")
                        .param("expirationDate", "01/01/2025"))
                .andExpect(MockMvcResultMatchers.status().isFound()); // 302 status code

        Equipment equipment = equipmentRepository.findAll().stream()
                .filter(e -> "Desk".equals(e.getEquipmentName()) &&
                        "Office Desk".equals(e.getEquipmentDescription()) &&
                        "Available".equals(e.getEquipmentStatus()) &&
                        "01/01/2025".equals(e.getExpirationDate()))
                .findFirst()
                .orElse(null);

        assert equipment != null;
        assert equipment.getEquipmentName().equals("Desk");
        assert equipment.getEquipmentDescription().equals("Office Desk");
        assert equipment.getEquipmentStatus().equals("Available");
        assert equipment.getExpirationDate().equals("01/01/2025");
    }

    @Test
    public void testMarkEquipmentAsDefective() throws Exception {
        User bob = new User("Bob", "password", "EMPLOYEE");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", bob);

        Equipment equipment = new Equipment("Printer", "HP LaserJet", "Available", "01/01/2024");
        equipmentRepository.save(equipment);

        mockMvc.perform(MockMvcRequestBuilders.post("/equipment/mark-defective/" + equipment.getEid()).session(session))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Equipment updatedEquipment = equipmentRepository.findById(equipment.getEid()).orElse(null);

        assert updatedEquipment != null;
        assert updatedEquipment.getEquipmentStatus().equals("Defective");
    }

    @Test
    public void testRemoveEquipment() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        Equipment equipment = new Equipment("Desk", "Office Desk", "Available", "01/01/2025");
        equipmentRepository.save(equipment);

        mockMvc.perform(MockMvcRequestBuilders.post("/equipment/remove/" + equipment.getEid()).session(session))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Equipment removedEquipment = equipmentRepository.findById(equipment.getEid()).orElse(null);
        assert removedEquipment == null;
    }

    @Test
    public void testUpdateExpiredEquipmentStatus() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        Equipment equipment = new Equipment("Monitor", "Samsung Monitor", "Expired", "01/01/2020");
        equipmentRepository.save(equipment);

        mockMvc.perform(MockMvcRequestBuilders.post("/equipment/update-status").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Equipment updatedEquipment = equipmentRepository.findById(equipment.getEid()).orElse(null);

        assert updatedEquipment != null;
        assert updatedEquipment.getEquipmentStatus().equals("Expired");
    }

    @Test
    public void testUpdateEquipmentStatus() throws Exception {
        User fred = new User("Fred", "password", "MANAGER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", fred);

        Equipment equipment = new Equipment("Chair", "Office Chair", "Expired", "01/01/2020");
        equipmentRepository.save(equipment);

        mockMvc.perform(MockMvcRequestBuilders.post("/equipment/update-expiration/" + equipment.getEid())
                        .session(session)
                        .param("expirationDate", "01/01/2025"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Equipment updatedEquipment = equipmentRepository.findById(equipment.getEid()).orElse(null);

        assert updatedEquipment != null;
        assert updatedEquipment.getExpirationDate().equals("01/01/2025");
        assert updatedEquipment.getEquipmentStatus().equals("Available");
    }
}
