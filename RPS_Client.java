import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RPS_Client extends JFrame {
    private JTextField nameField;
    private JButton connectButton;
    private JComboBox<String> modeComboBox;
    private JButton rockButton, paperButton, scissorsButton, quitButton;
    private JTextArea gameLog;
    private BufferedReader in;
    private BufferedWriter out;
    private Socket socket;

    public RPS_Client() {
        setTitle("Rock-Paper-Scissors Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 2));
        nameField = new JTextField();
        connectButton = new JButton("Connect");
        modeComboBox = new JComboBox<>(new String[]{"Play with Bot", "Play with another Player"});

        panel.add(new JLabel("Enter your name:"));
        panel.add(nameField);
        panel.add(new JLabel("Choose game mode:"));
        panel.add(modeComboBox);
        panel.add(connectButton);

        add(panel, BorderLayout.NORTH);

        gameLog = new JTextArea();
        gameLog.setEditable(false);
        add(new JScrollPane(gameLog), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));

        rockButton = new JButton(resizeIcon("images/rock.png", 70, 70));
        paperButton = new JButton(resizeIcon("images/paper.png", 70, 70));
        scissorsButton = new JButton(resizeIcon("images/scissors.png", 70, 70));
        quitButton = new JButton(resizeIcon("images/quit.png", 70, 70));

        buttonPanel.add(rockButton);
        buttonPanel.add(paperButton);
        buttonPanel.add(scissorsButton);
        buttonPanel.add(quitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        connectButton.addActionListener(new ConnectButtonListener());
        rockButton.addActionListener(new MoveButtonListener("ROCK"));
        paperButton.addActionListener(new MoveButtonListener("PAPER"));
        scissorsButton.addActionListener(new MoveButtonListener("SCISSORS"));
        quitButton.addActionListener(new MoveButtonListener("Q"));
    }

    private ImageIcon resizeIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private class ConnectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String ipAddress = InetAddress.getLocalHost().getHostAddress();
                int portNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter server port:"));
                socket = new Socket(ipAddress, portNumber);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String playerName = nameField.getText();
                out.write(playerName + "\n");
                out.flush();

                String mode = modeComboBox.getSelectedIndex() == 0 ? "1" : "2";
                out.write(mode + "\n");
                out.flush();

                gameLog.append("Connected to server.\n");
                gameLog.append("Game mode: " + (mode.equals("1") ? "Play with Bot" : "Play with another Player") + "\n");

                new Thread(new ServerListener()).start();
            } catch (UnknownHostException ex) {
                gameLog.append("Unknown host.\n");
            } catch (IOException ex) {
                gameLog.append("Unable to connect to server.\n");
            }
        }
    }

    private class MoveButtonListener implements ActionListener {
        private String move;

        public MoveButtonListener(String move) {
            this.move = move;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                out.write(move + "\n");
                out.flush();
                if (move.equalsIgnoreCase("Q")) {
                    gameLog.append("You have exited the game.\n");
                    socket.close();
                } else {
                    gameLog.append("You chose: " + move + "\n");
                }
            } catch (IOException ex) {
                gameLog.append("Error sending move.\n");
            }
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    gameLog.append(response + "\n");
                }
            } catch (IOException ex) {
                gameLog.append("Connection closed.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RPS_Client clientGUI = new RPS_Client();
            clientGUI.setVisible(true);
        });
    }
}
