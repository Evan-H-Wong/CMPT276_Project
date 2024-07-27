package group8.skyweaver_inventory.models;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eid;
    private String equipmentName;
    private String equipmentDescription;
    private String equipmentStatus;

    @Column(name = "expiration_date")
    private String expirationDate;

    public Equipment() {
    }

    public Equipment(String equipmentName, String equipmentDescription, String equipmentStatus, String expirationDate) {
        this.equipmentName = equipmentName;
        this.equipmentDescription = equipmentDescription;
        this.equipmentStatus = equipmentStatus;
        this.expirationDate = expirationDate;
    }

    public int getEid() {
        return eid;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getEquipmentDescription() {
        return equipmentDescription;
    }

    public String getEquipmentStatus() {
        return equipmentStatus;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public void setEquipmentDescription(String equipmentDescription) {
        this.equipmentDescription = equipmentDescription;
    }

    public void setEquipmentStatus(String equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
