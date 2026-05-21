package yu.aihackerton.fingerbot.dto;

public class ChatResponseDto {
    private String reply;
    private Integer suggestedStep;

    public ChatResponseDto(String reply) { this.reply = reply; }
    public ChatResponseDto(String reply, Integer suggestedStep) {
        this.reply = reply;
        this.suggestedStep = suggestedStep;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public Integer getSuggestedStep() { return suggestedStep; }
    public void setSuggestedStep(Integer suggestedStep) { this.suggestedStep = suggestedStep; }
}
