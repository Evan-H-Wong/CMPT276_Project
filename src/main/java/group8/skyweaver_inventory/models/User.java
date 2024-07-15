package group8.skyweaver_inventory.models;

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
    private String accesslevel; // either Manager or Employee

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Message> messages;

    public User() {
    }

    public User(String username, String password, String accesslevel) {
        this.username = username;
        this.password = password;
        this.accesslevel = accesslevel;
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
}
