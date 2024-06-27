package group8.skyweaver_inventory.models;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;
    private String username;
    private String password;
    private String Id; // either Manager or Employee

    public User() {
    }

    public User(String username, String password, String Id) {
        this.username = username;
        this.password = password;
        this.Id = Id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return Id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(String Id) {
        this.Id = Id;
    }
}
