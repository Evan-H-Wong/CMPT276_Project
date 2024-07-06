package group8.skyweaver_inventory.models;

import jakarta.persistence.*;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String messageName;
    private String messageContent;
    private String timeSent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Message() {
    }

    public Message(String messageName, String messageContent, String timeSent) {
        this.messageName = messageName;
        this.messageContent = messageContent;
        this.timeSent = timeSent;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }
}
