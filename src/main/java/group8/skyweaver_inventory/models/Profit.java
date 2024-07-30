package group8.skyweaver_inventory.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "profit")
public class Profit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;
    
    // LocalDateTime.of(year, month, dayOfMonth, hour, minute, second)
    private LocalDateTime date;
    private Double profit;
    
    public Profit(){
    }

    public Profit(LocalDateTime date, Double profit) {
        this.date = date;
        this.profit = profit;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }
    
}
