package CoolChat;

import javax.swing.SwingUtilities;

public class ChatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatWindow();
        });
    }
}
