import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class RPS_Sever {
    private static ArrayList<MultiplayerRoom> rooms = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Get port number from the user
        System.out.print("Enter port number: ");
        Scanner scan = new Scanner(System.in);
        int portNumber = scan.nextInt();

        // Create server socket and start listening on the given port
        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Server started and listening to port " + portNumber);

        // Continuously accept new client connections
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    // Handles client connections
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

                // Get player name
                // out.write("Enter your name: ");
                // out.flush();
                String playerName = in.readLine();

                // Get game mode choice
                // out.write("Choose game mode (1: Play with Bot, 2: Play with another Player): ");
                // out.flush();
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

        // Handles gameplay against the bot
        private void playWithBot(String playerName) throws IOException {
            String[] choices = {"ROCK", "PAPER", "SCISSORS"};
            Random random = new Random();

            while (true) {
                // out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
                // out.flush();
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

        // Handles joining or creating a multiplayer game
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

        // Determines the winner of a Rock-Paper-Scissors round
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

    // Manages a multiplayer game room
    private static class MultiplayerRoom {
        private Socket player1Socket;
        private String player1Name;
        private Socket player2Socket;
        private String player2Name;
        private BufferedReader player1In;
        private BufferedWriter player1Out;
        private BufferedReader player2In;
        private BufferedWriter player2Out;

        // Adds a player to the room
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

        // Checks if the room is waiting for a second player
        public boolean isWaitingForPlayer() {
            return player2Socket == null;
        }

        // Starts the multiplayer game
// Inside the MultiplayerRoom class
        private void startGame() {
            try {
                player1Out.write("Another player has joined. Let's start!\n");
                player1Out.flush();
                player2Out.write("Let's start!\n");
                player2Out.flush();

                while (true) {
                    // player1Out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
                    // player1Out.flush();
                    String player1Move = player1In.readLine().toUpperCase();

                    // player2Out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
                    // player2Out.flush();
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


        // Determines the winner of a Rock-Paper-Scissors round
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
}




