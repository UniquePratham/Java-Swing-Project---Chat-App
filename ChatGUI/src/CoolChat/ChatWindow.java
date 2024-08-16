package CoolChat;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatWindow extends JFrame {
	private JTextArea chatArea;
	private JTextField messageField;
	private JButton sendButton;
	private JList<String> usersList;
	private DefaultListModel<String> listModel;
	private Map<String, List<String>> messageMap;
	private String lastSender = "";
	private String lastChattingWith = "";
	private JLabel chatPersonLabel;
	private JPanel placeholderPanel;
	private JLabel placeholderLabel1;
	private JLabel placeholderLabel2;

	public ChatWindow() {
		setTitle("CoolChat");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (screenSize.getWidth()), (int) (screenSize.getHeight() / 1.1));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("CoolChat\\Resources\\ChatIcon.png"));
		setIconImage(icon.getImage());
		getContentPane().setBackground(new Color(60, 63, 65));

		// Chat area
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setBackground(new Color(43, 43, 43));
		chatArea.setForeground(Color.WHITE);
		chatArea.setFont(new Font("Arial", Font.PLAIN, 16));
		chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JScrollPane chatScrollPane = new JScrollPane(chatArea);
		add(chatScrollPane, BorderLayout.CENTER);

		// Placeholder Panel
		placeholderPanel = new JPanel(new BorderLayout());
		placeholderPanel.setBackground(new Color(43, 43, 43));
		// Placeholder label
		placeholderLabel1 = new JLabel("Welcome to CoolChat!", SwingConstants.CENTER);
		placeholderLabel1.setFont(new Font("Arial", Font.BOLD, 35));
		placeholderLabel1.setForeground(Color.GRAY);
		placeholderPanel.add(placeholderLabel1, BorderLayout.NORTH);
		placeholderLabel2 = new JLabel("Select any person to chat", SwingConstants.CENTER);
		placeholderLabel2.setFont(new Font("Arial", Font.BOLD, 35));
		placeholderLabel2.setForeground(Color.GRAY);
		placeholderPanel.add(placeholderLabel2, BorderLayout.CENTER);
		add(placeholderPanel, BorderLayout.CENTER);

		// Message field and Send button
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messagePanel.setBackground(new Color(60, 63, 65));
		messageField = new JTextField(20);
		final String placeholderText = "Enter your message here...";
		messageField.setText(placeholderText);
		messageField.setFont(new Font("Arial", Font.PLAIN, 16));
		messageField.setBackground(new Color(69, 73, 74));
		messageField.setForeground(Color.GRAY);
		messageField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (messageField.getText().equals(placeholderText)) {
					messageField.setText("");
					messageField.setForeground(Color.WHITE);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (messageField.getText().isEmpty()) {
					messageField.setForeground(Color.GRAY);
					messageField.setText(placeholderText);
				}
			}
		});

		// Add key listener for Enter key
		messageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});

		messagePanel.add(messageField, BorderLayout.CENTER);

		sendButton = new JButton("Send");
		sendButton.setFont(new Font("Arial", Font.BOLD, 16));
		sendButton.setBackground(new Color(98, 144, 221));
		sendButton.setForeground(Color.WHITE);
		messagePanel.add(sendButton, BorderLayout.EAST);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});
		add(messagePanel, BorderLayout.SOUTH);

		// Users list
		listModel = new DefaultListModel<>();
		loadChatPersons();
		usersList = new JList<>(listModel);
		usersList.setBackground(new Color(43, 43, 43));
		usersList.setForeground(new Color(98, 144, 221));
		usersList.setFont(new Font("Arial", Font.PLAIN, 16));
		usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usersList.setFixedCellHeight(30);
		usersList.setLayoutOrientation(JList.VERTICAL);
		usersList.addListSelectionListener(e -> {
			String selectedPerson = usersList.getSelectedValue();
			if (selectedPerson != null) {
				displayMessages(selectedPerson);
			}
		});
		JScrollPane usersScrollPane = new JScrollPane(usersList);
		usersScrollPane.setPreferredSize(new Dimension(200, getHeight()));
		add(usersScrollPane, BorderLayout.EAST);

		// Close button and title
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(new Color(60, 63, 65));
		chatPersonLabel = new JLabel("CoolChat");
		chatPersonLabel.setFont(new Font("Arial", Font.BOLD, 18));
		chatPersonLabel.setForeground(new Color(98, 144, 221));
		topPanel.add(chatPersonLabel, BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.setFont(new Font("Arial", Font.BOLD, 16));
		closeButton.setBackground(new Color(255, 69, 58));
		closeButton.setForeground(Color.WHITE);
		closeButton.addActionListener(e -> dispose());
		topPanel.add(closeButton, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		setVisible(true);

		// Initialize message maps
		messageMap = new HashMap<>();

		// Load messages from files
		loadMessages();

		// Adjust user list width
		adjustUsersListWidth();
	}

	private void loadChatPersons() {
		File file = new File("src\\CoolChat\\Resources\\chatpersonslist.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				listModel.addElement(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadMessages() {
		File file = new File("src\\CoolChat\\Resources\\messages.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			String currentPerson = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("-- ")) {
					currentPerson = line.substring(3).trim();
					lastChattingWith = currentPerson;
				} else if (!line.isEmpty() && currentPerson != null) {
					messageMap.computeIfAbsent(currentPerson, k -> new ArrayList<>()).add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void displayMessages(String person) {
		chatArea.setText("");
		lastSender = "";
		if (placeholderPanel.isVisible()) {
			remove(placeholderPanel);
			add(new JScrollPane(chatArea), BorderLayout.CENTER);
			revalidate();
		}
		chatPersonLabel.setText(person);

		List<String> messages = messageMap.get(person);
		if (messages != null) {
			for (String message : messages) {
				String[] individualMessages = message.split("\n");
				for (String msg : individualMessages) {
					if (msg.startsWith("You : ")) {
						if (!lastSender.equals("You")) {
							lastSender = "You";
							chatArea.append(msg + "\n");
						} else {
							String newMessage = msg.replaceFirst("You : ", "");
							chatArea.append(newMessage + "\n");
						}
					} else {
						if (lastSender.equals(person)) {
							chatArea.append(msg + "\n");
						} else {
							chatArea.append(person + " : " + msg + "\n");
						}
						lastSender = person;
					}
				}
			}
		}
	}

	private void sendMessage() {
		String selectedPerson = usersList.getSelectedValue();
		if (selectedPerson == null) {
			JOptionPane.showMessageDialog(this, "Select a person to chat with.", "No Person Selected",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String message = messageField.getText().trim();
		if (!message.isEmpty() && !message.equals("Enter your message here...")) {
			String formattedMessage = (lastSender.equals("You")) ? message : "You : " + message;
			if (placeholderPanel.isVisible()) {
				remove(placeholderPanel);
				add(new JScrollPane(chatArea), BorderLayout.CENTER);
				revalidate();
			}
			chatArea.append(formattedMessage + "\n");
			messageMap.computeIfAbsent(selectedPerson, k -> new ArrayList<>()).add("You : " + message);
			messageField.setText("");
			saveMessageToFile(selectedPerson, "You : " + message);
			playSendSound();
			lastSender = "You";
			chatPersonLabel.setText(selectedPerson);
		} else {
			JOptionPane.showMessageDialog(this, "Please Type Something to Send.", "Message Box can't be Empty",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
	}

	private void saveMessageToFile(String person, String message) {
		File file = new File("src\\CoolChat\\Resources\\messages.txt");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			if (!person.equals(lastChattingWith)) {
				writer.write("\n-- " + person);
			}
			writer.write("\n" + message);
			lastChattingWith = person;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void playSendSound() {
		try (InputStream audioSrc = getClass().getClassLoader()
				.getResourceAsStream("CoolChat\\Resources\\Send_Sound.wav")) {
			if (audioSrc == null) {
				throw new NullPointerException("Audio resource not found");
			}
			InputStream bufferedIn = new BufferedInputStream(audioSrc);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void adjustUsersListWidth() {
		int maxWidth = 0;
		for (int i = 0; i < listModel.size(); i++) {
			String user = listModel.get(i);
			FontMetrics fm = usersList.getFontMetrics(usersList.getFont());
			int width = fm.stringWidth(user);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		int padding = 30;
		int preferredWidth = maxWidth + padding;
		usersList.setFixedCellWidth(preferredWidth);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new ChatWindow());
	}
}
