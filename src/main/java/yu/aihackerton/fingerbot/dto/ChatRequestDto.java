package yu.aihackerton.fingerbot.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatRequestDto {
    private List<MessageDto> messages = new ArrayList<>();
    private String newMessage;

    public List<MessageDto> getMessages() { return messages; }
    public void setMessages(List<MessageDto> messages) { this.messages = messages; }
    public String getNewMessage() { return newMessage; }
    public void setNewMessage(String newMessage) { this.newMessage = newMessage; }
}
