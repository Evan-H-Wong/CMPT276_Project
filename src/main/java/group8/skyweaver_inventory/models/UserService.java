package group8.skyweaver_inventory.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    public User getUserWithMessages(int userId) {
        return userRepository.findById(userId);
    }

    public Message getMessageById(int id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }
}
