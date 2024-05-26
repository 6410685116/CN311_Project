import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class RPS_Server extends JFrame {
    private static ArrayList<MultiplayerRoom> rooms = new ArrayList<>();
    private ServerSocket serverSocket;
    private boolean running = false;
    private JButton toggleButton;
    private JTextField portField;
    private JTextArea logArea;
    private Thread serverThread;

    public RPS_Server() {
        setTitle("Rock-Paper-Scissors Server");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        controlPanel.add(new JLabel("Port Number:"));
        portField = new JTextField(5);  
        portField.setMaximumSize(new Dimension(300, 30));
        controlPanel.add(portField);
        toggleButton = new JButton("Start Server");

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleServer();
            }
        });

        controlPanel.add(toggleButton);

        add(controlPanel, BorderLayout.WEST);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    private void toggleServer() {
        if (running) {
            stopServer();
        } else {
            startServer();
        }
    }

    private void startServer() {
        try {
            int portNumber = Integer.parseInt(portField.getText());
            serverSocket = new ServerSocket(portNumber);
            running = true;
            toggleButton.setText("Stop Server");
            logArea.append("Server started and listening on port " + portNumber + "\n");

            serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            new Thread(new ClientHandler(clientSocket)).start();
                        } catch (IOException e) {
                            if (running) {
                                logArea.append("Error accepting connection: " + e.getMessage() + "\n");
                            }
                        }
                    }
                }
            });
            serverThread.start();
        } catch (NumberFormatException e) {
            logArea.append("Invalid port number.\n");
        } catch (IOException e) {
            logArea.append("Error starting server: " + e.getMessage() + "\n");
        }
    }

    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (serverThread != null) {
                serverThread.join();
            }
            toggleButton.setText("Start Server");
            logArea.append("Server stopped.\n");
        } catch (IOException | InterruptedException e) {
            logArea.append("Error stopping server: " + e.getMessage() + "\n");
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private BufferedWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String playerName = in.readLine();
                String mode = in.readLine();

                if ("1".equals(mode)) {
                    playWithBot(playerName);
                } else if ("2".equals(mode)) {
                    joinMultiplayerGame(playerName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void playWithBot(String playerName) throws IOException {
            String[] choices = {"ROCK", "PAPER", "SCISSORS"};
            Random random = new Random();

            while (true) {
                String playerMove = in.readLine().toUpperCase();

                if ("Q".equalsIgnoreCase(playerMove)) {
                    out.write("You have exited the game.\n");
                    out.flush();
                    break;
                }

                String botMove = choices[random.nextInt(choices.length)];
                String result = determineWinner(playerMove, botMove);
                result = result.replace("Player 1", playerName).replace("Player 2", "Bot");

                out.write("Bot chose: " + botMove + " Results: " + result + "\n");
                out.flush();
            }
        }

        private void joinMultiplayerGame(String playerName) throws IOException {
            MultiplayerRoom availableRoom = null;
            for (MultiplayerRoom room : rooms) {
                if (room.isWaitingForPlayer()) {
                    availableRoom = room;
                    break;
                }
            }

            if (availableRoom == null) {
                availableRoom = new MultiplayerRoom();
                rooms.add(availableRoom);
            }

            availableRoom.addPlayer(clientSocket, playerName);
        }

        private String determineWinner(String move1, String move2) {
            if (move1.equalsIgnoreCase(move2)) {
                return "It's a tie!";
            } else if ((move1.equalsIgnoreCase("ROCK") && move2.equalsIgnoreCase("SCISSORS")) ||
                       (move1.equalsIgnoreCase("PAPER") && move2.equalsIgnoreCase("ROCK")) ||
                       (move1.equalsIgnoreCase("SCISSORS") && move2.equalsIgnoreCase("PAPER"))) {
                return "Player 1 wins!";
            } else {
                return "Player 2 wins!";
            }
        }
    }

    private static class MultiplayerRoom {
        private Socket player1Socket;
        private String player1Name;
        private Socket player2Socket;
        private String player2Name;
        private BufferedReader player1In;
        private BufferedWriter player1Out;
        private BufferedReader player2In;
        private BufferedWriter player2Out;

        public synchronized void addPlayer(Socket playerSocket, String playerName) throws IOException {
            if (player1Socket == null) {
                player1Socket = playerSocket;
                player1Name = playerName;
                player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
                player1Out = new BufferedWriter(new OutputStreamWriter(player1Socket.getOutputStream()));
                player1Out.write("Waiting for another player to join...\n");
                player1Out.flush();
            } else { 
                player2Socket = playerSocket;
                player2Name = playerName;
                player2In = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
                player2Out = new BufferedWriter(new OutputStreamWriter(player2Socket.getOutputStream()));
                player2Out.write("You have joined the game.\n");
                player2Out.flush();

                new Thread(this::startGame).start();
            }
        }

        public boolean isWaitingForPlayer() {
            return player2Socket == null;
        }

        private void startGame() {
            try {
                player1Out.write( player2Name + " has joined. Let's start!\n");
                player1Out.flush();
                player2Out.write("play with " + player1Name + ". Let's start!\n");
                player2Out.flush();

                while (true) {
                    String player1Move = player1In.readLine().toUpperCase();
                    String player2Move = player2In.readLine().toUpperCase();

                    if ("Q".equalsIgnoreCase(player1Move) || "Q".equalsIgnoreCase(player2Move)) {
                        player1Out.write("Game over.\n");
                        player1Out.flush();
                        player2Out.write("Game over.\n");
                        player2Out.flush();
                        break;
                    }

                    String result = determineWinner(player1Move, player2Move);
                    result = result.replace("Player 1", player1Name).replace("Player 2", player2Name);

                    player1Out.write(player2Name + " chose: " + player2Move + "\n" + result + "\n");
                    player1Out.flush();
                    player2Out.write(player1Name + " chose: " + player1Move + "\n" + result + "\n");
                    player2Out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (player1Socket != null) player1Socket.close();
                    if (player2Socket != null) player2Socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private String determineWinner(String move1, String move2) {
            if (move1.equalsIgnoreCase(move2)) {
                return "It's a tie!";
            } else if ((move1.equalsIgnoreCase("ROCK") && move2.equalsIgnoreCase("SCISSORS")) ||
                       (move1.equalsIgnoreCase("PAPER") && move2.equalsIgnoreCase("ROCK")) ||
                       (move1.equalsIgnoreCase("SCISSORS") && move2.equalsIgnoreCase("PAPER"))) {
                return "Player 1 wins!";
            } else {
                return "Player 2 wins!";
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RPS_Server serverGUI = new RPS_Server();
            serverGUI.setVisible(true);
        });
    }
}
