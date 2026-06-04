package yu.aihackerton.fingerbot.dto;

public class ChatResponseDto {
    private String reply;
    private boolean settingsReady;
    private ChatSettingsDto settings;

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public boolean isSettingsReady() { return settingsReady; }
    public void setSettingsReady(boolean settingsReady) { this.settingsReady = settingsReady; }
    public ChatSettingsDto getSettings() { return settings; }
    public void setSettings(ChatSettingsDto settings) { this.settings = settings; }
}
