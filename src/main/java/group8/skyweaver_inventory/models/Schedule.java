package group8.skyweaver_inventory.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // LocalDateTime.of(year, month, dayOfMonth, hour, minute, second)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean Weekly;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user; // Reference to the User entity 
    
    public Schedule(){
    }

    public Schedule(Long id, LocalDateTime startTime, LocalDateTime endTime, Boolean weekly, User user) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.Weekly = weekly;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Boolean getWeekly() {
        return Weekly;
    }

    public void setWeekly(Boolean weekly) {
        Weekly = weekly;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    
}
