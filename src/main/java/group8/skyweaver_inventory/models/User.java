package group8.skyweaver_inventory.models;

//import java.util.ArrayList;
//import java.util.List;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;
    private String username;
    private String password;
    private String accesslevel;
    private Boolean isAvailable;
    private String gmail;
    private String token;
    private String refreshToken;
    private double salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Message> messages;

    public User() {
    }

    public User(String username, String password, String accesslevel) {
        this.username = username;
        this.password = password;
        this.accesslevel = accesslevel;
        this.isAvailable = true;
        this.salary = 0.0;
        this.messages = new ArrayList<>();
        addWelcomeMessage();
    }
    

    public int getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccesslevel() {
        return accesslevel;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int numberMessages() {
        return this.messages.size();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccesslevel(String accesslevel) {
        this.accesslevel = accesslevel;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    private void addWelcomeMessage() {
        Message welcomeMessage = new Message("Welcome", "Welcome to the Company",
                LocalDateTime.now().toString(), "Management", this);
        this.messages.add(welcomeMessage);
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}
