// import java.io.*;
// import java.net.*;
// import java.util.ArrayList;
// import java.util.Random;
// import java.util.Scanner;

// public class RockPaperScissorsServer2 {

//     public static void main(String[] args) {
//         System.out.print("Enter port number: ");
//         Scanner scan = new Scanner(System.in);
//         int portNumber = Integer.parseInt(scan.nextLine());

//         try {
//             ServerSocket server = new ServerSocket(portNumber);
//             System.out.println("Server Started and listening to port " + portNumber);

//             System.out.print("Enter 1 to play with bot, 2 to play with player: ");
//             int gameMode = Integer.parseInt(scan.nextLine());

//             while (true) {
//                 if (gameMode == 1) {
//                     Socket clientSocket = server.accept();
//                     new Thread(new Singleplay(clientSocket)).start();
//                 } else if (gameMode == 2) {
//                     System.out.println("Waiting for Player 1 to connect...");
//                     Socket player1Socket = server.accept();
//                     System.out.println("Player 1 connected");

//                     System.out.println("Waiting for Player 2 to connect...");
//                     Socket player2Socket = server.accept();
//                     System.out.println("Player 2 connected");

//                     new Thread(new Multiplay(player1Socket, player2Socket)).start();
//                 } else {
//                     System.out.println("Invalid game mode. Enter 1 or 2.");
//                 }
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     public static class Singleplay implements Runnable {
//         private Socket clientSocket;

//         public Singleplay(Socket clientSocket) {
//             this.clientSocket = clientSocket;
//         }

//         @Override
//         public void run() {
//             try {
//                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

//                 Random rand = new Random();
//                 String[] options = {"ROCK", "PAPER", "SCISSORS"};

//                 while (true) {
//                     String clientMove = reader.readLine();
//                     if (clientMove.equalsIgnoreCase("q")) {
//                         break;
//                     }

//                     String botMove = options[rand.nextInt(options.length)];
//                     String result = determineWinner(clientMove, botMove);
//                     writer.write(result + "\n");
//                     writer.flush();
//                 }

//                 clientSocket.close();
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     public static class Multiplay implements Runnable {
//         private Socket player1Socket;
//         private Socket player2Socket;

//         public Multiplay(Socket player1Socket, Socket player2Socket) {
//             this.player1Socket = player1Socket;
//             this.player2Socket = player2Socket;
//         }

//         @Override
//         public void run() {
//             try {
//                 BufferedReader player1Reader = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
//                 BufferedWriter player1Writer = new BufferedWriter(new OutputStreamWriter(player1Socket.getOutputStream()));
//                 BufferedReader player2Reader = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
//                 BufferedWriter player2Writer = new BufferedWriter(new OutputStreamWriter(player2Socket.getOutputStream()));

//                 while (true) {
//                     String player1Move = player1Reader.readLine();
//                     String player2Move = player2Reader.readLine();

//                     if (player1Move.equalsIgnoreCase("q") || player2Move.equalsIgnoreCase("q")) {
//                         break;
//                     }

//                     String result = determineWinner(player1Move, player2Move);

//                     player1Writer.write(result + "\n");
//                     player1Writer.flush();

//                     player2Writer.write(result + "\n");
//                     player2Writer.flush();
//                 }

//                 player1Socket.close();
//                 player2Socket.close();
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     private static String determineWinner(String move1, String move2) {
//         if (move1.equalsIgnoreCase(move2)) {
//             return "It's a tie!";
//         } else if ((move1.equalsIgnoreCase("ROCK") && move2.equalsIgnoreCase("SCISSORS")) ||
//                 (move1.equalsIgnoreCase("PAPER") && move2.equalsIgnoreCase("ROCK")) ||
//                 (move1.equalsIgnoreCase("SCISSORS") && move2.equalsIgnoreCase("PAPER"))) {
//             return "Player 1 wins!";
//         } else {
//             return "Player 2 wins!";
//         }
//     }
// }

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class RockPaperScissorsServer2 {
//     private static final String[] MOVES = {"ROCK", "PAPER", "SCISSORS"};
//     private int playerCount = 0;
//     private String player1Name;
//     private String player2Name;
//     private BufferedWriter player1Out;
//     private BufferedWriter player2Out;

//     public static void main(String[] args) {
//         new RockPaperScissorsServer2().startServer();
//     }

//     public void startServer() {
//         try (ServerSocket serverSocket = new ServerSocket(12345)) {
//             System.out.println("Server started and listening on port 12345...");
//             while (true) {
//                 Socket clientSocket = serverSocket.accept();
//                 System.out.println("Client connected: " + clientSocket);

//                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

//                 String playerName = in.readLine();
//                 System.out.println("Player " + playerName + " joined.");

//                 if (playerCount == 0) {
//                     player1Name = playerName;
//                     player1Out = out;
//                 } else {
//                     player2Name = playerName;
//                     player2Out = out;
//                 }

//                 playerCount++;

//                 if (playerCount == 2) {
//                     playMultiplayer(player1Name, player1Out, player2Name, player2Out);
//                 } else {
//                     out.write("Waiting for another player to join...\n");
//                     out.flush();
//                 }
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     private void playMultiplayer(String player1Name, BufferedWriter player1Out, String player2Name, BufferedWriter player2Out) throws IOException {
//         player1Out.write("Another player joined. Starting the game...\n");
//         player1Out.flush();
//         player2Out.write("Another player joined. Starting the game...\n");
//         player2Out.flush();

//         // Generate moves for both players
//         String player1Move = generateMove();
//         String player2Move = generateMove();

//         // Determine the winner
//         String result = determineWinner(player1Move, player2Move);

//         // Send the result to both players
//         sendResultToPlayer(player1Name, player1Move, result, player1Out);
//         sendResultToPlayer(player2Name, player2Move, result, player2Out);

//         // Reset player count
//         playerCount = 0;
//     }

//     private void sendResultToPlayer(String playerName, String playerMove, String result, BufferedWriter out) throws IOException {
//         out.write("Result: " + playerName + " chose " + playerMove + ". " + result + "\n");
//         out.flush();
//     }

//     private String generateMove() {
//         // Generate a random move
//         Random random = new Random();
//         int moveIndex = random.nextInt(3);
//         return MOVES[moveIndex];
//     }

//     private String determineWinner(String move1, String move2) {
//         // Logic to determine the winner
//         if (move1.equals(move2)) {
//             return "It's a tie!";
//         } else if ((move1.equals("ROCK") && move2.equals("SCISSORS")) ||
//                    (move1.equals("PAPER") && move2.equals("ROCK")) ||
//                    (move1.equals("SCISSORS") && move2.equals("PAPER"))) {
//             return "Player 1 wins!";
//         } else {
//             return "Player 2 wins!";
//         }
//     }
// }

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class RockPaperScissorsServer2 {

//     private static final int PORT = 12345;
//     private static final Map<Integer, Room> rooms = new HashMap<>();
//     private static final Object roomLock = new Object();
//     private static int roomCounter = 0;

//     public static void main(String[] args) {
//         System.out.println("Server started and listening on port " + PORT);

//         try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//             while (true) {
//                 Socket clientSocket = serverSocket.accept();
//                 new Thread(new ClientHandler(clientSocket)).start();
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     private static class Room {
//         private final int id;
//         private final List<ClientHandler> players = new ArrayList<>();

//         public Room(int id) {
//             this.id = id;
//         }

//         public int getId() {
//             return id;
//         }

//         public List<ClientHandler> getPlayers() {
//             return players;
//         }

//         public boolean isFull() {
//             return players.size() >= 2;
//         }

//         public void addPlayer(ClientHandler player) {
//             players.add(player);
//         }

//         public void removePlayer(ClientHandler player) {
//             players.remove(player);
//         }
//     }

//     private static class ClientHandler implements Runnable {
//         private final Socket socket;
//         private String playerName;
//         private BufferedReader in;
//         private BufferedWriter out;
//         private Room currentRoom;
//         private boolean isPlayingWithBot;

//         public ClientHandler(Socket socket) {
//             this.socket = socket;
//         }

//         @Override
//         public void run() {
//             try {
//                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                 out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

//                 out.write("Enter your name: ");
//                 out.flush();
//                 playerName = in.readLine();
//                 System.out.println("Player " + playerName + " joined.");

//                 out.write("Choose game mode (1: Play with Bot, 2: Play with another Player): ");
//                 out.flush();
//                 String mode = in.readLine();

//                 if ("1".equals(mode)) {
//                     isPlayingWithBot = true;
//                     playWithBot();
//                 } else if ("2".equals(mode)) {
//                     isPlayingWithBot = false;
//                     joinMultiplayerRoom();
//                 } else {
//                     out.write("Invalid option. Disconnecting...\n");
//                     out.flush();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             } finally {
//                 closeConnection();
//             }
//         }

//         private void playWithBot() throws IOException {
//             while (true) {
//                 out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//                 out.flush();
//                 String playerMove = in.readLine().toUpperCase();

//                 if ("Q".equals(playerMove)) {
//                     out.write("You have quit the game.\n");
//                     out.flush();
//                     break;
//                 }

//                 String botMove = generateBotMove();
//                 String result = determineWinner(playerName, "Bot", playerMove, botMove);

//                 out.write("Result: " + result + "\n");
//                 out.flush();
//             }
//         }

//         private void joinMultiplayerRoom() throws IOException {
//             synchronized (roomLock) {
//                 for (Room room : rooms.values()) {
//                     if (!room.isFull()) {
//                         currentRoom = room;
//                         break;
//                     }
//                 }

//                 if (currentRoom == null) {
//                     currentRoom = new Room(++roomCounter);
//                     rooms.put(currentRoom.getId(), currentRoom);
//                 }

//                 currentRoom.addPlayer(this);

//                 if (currentRoom.isFull()) {
//                     startMultiplayerGame();
//                 } else {
//                     out.write("Waiting for another player to join...\n");
//                     out.flush();
//                 }
//             }
//         }

//         private void startMultiplayerGame() throws IOException {
//             List<ClientHandler> players = currentRoom.getPlayers();
//             ClientHandler player1 = players.get(0);
//             ClientHandler player2 = players.get(1);

//             player1.out.write("Another player joined. Starting the game...\n");
//             player1.out.flush();
//             player2.out.write("Another player joined. Starting the game...\n");
//             player2.out.flush();

//             while (true) {
//                 String player1Move = getPlayerMove(player1);
//                 if (player1Move == null) break;
//                 String player2Move = getPlayerMove(player2);
//                 if (player2Move == null) break;

//                 String result = determineWinner(player1.playerName, player2.playerName, player1Move, player2Move);

//                 player1.out.write("Result: " + result + "\n");
//                 player1.out.flush();
//                 player2.out.write("Result: " + result + "\n");
//                 player2.out.flush();
//             }

//             currentRoom.removePlayer(player1);
//             currentRoom.removePlayer(player2);
//         }

//         private String getPlayerMove(ClientHandler player) throws IOException {
//             player.out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//             player.out.flush();
//             String move = player.in.readLine().toUpperCase();

//             if ("Q".equals(move)) {
//                 player.out.write("You have quit the game.\n");
//                 player.out.flush();
//                 return null;
//             }

//             return move;
//         }

//         private String generateBotMove() {
//             String[] moves = {"ROCK", "PAPER", "SCISSORS"};
//             Random random = new Random();
//             return moves[random.nextInt(moves.length)];
//         }

//         private String determineWinner(String player1Name, String player2Name, String player1Move, String player2Move) {
//             if (player1Move.equals(player2Move)) {
//                 return "It's a tie!";
//             }

//             switch (player1Move) {
//                 case "ROCK":
//                     return player2Move.equals("SCISSORS") ? player1Name + " wins!" : player2Name + " wins!";
//                 case "PAPER":
//                     return player2Move.equals("ROCK") ? player1Name + " wins!" : player2Name + " wins!";
//                 case "SCISSORS":
//                     return player2Move.equals("PAPER") ? player1Name + " wins!" : player2Name + " wins!";
//                 default:
//                     return "Invalid move!";
//             }
//         }

//         private void closeConnection() {
//             try {
//                 if (currentRoom != null) {
//                     currentRoom.removePlayer(this);
//                 }
//                 if (in != null) {
//                     in.close();
//                 }
//                 if (out != null) {
//                     out.close();
//                 }
//                 if (socket != null) {
//                     socket.close();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class RockPaperScissorsServer2 {

//     private static final int PORT = 12345;
//     private static final Map<Integer, Room> rooms = new HashMap<>();
//     private static final Object roomLock = new Object();
//     private static int roomCounter = 0;

//     public static void main(String[] args) {
//         System.out.println("Server started and listening on port " + PORT);

//         try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//             while (true) {
//                 Socket clientSocket = serverSocket.accept();
//                 new Thread(new ClientHandler(clientSocket)).start();
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     private static class Room {
//         private final int id;
//         private final List<ClientHandler> players = new ArrayList<>();

//         public Room(int id) {
//             this.id = id;
//         }

//         public int getId() {
//             return id;
//         }

//         public List<ClientHandler> getPlayers() {
//             return players;
//         }

//         public boolean isFull() {
//             return players.size() >= 2;
//         }

//         public void addPlayer(ClientHandler player) {
//             players.add(player);
//         }

//         public void removePlayer(ClientHandler player) {
//             players.remove(player);
//         }
//     }

//     private static class ClientHandler implements Runnable {
//         private final Socket socket;
//         private String playerName;
//         private BufferedReader in;
//         private BufferedWriter out;
//         private Room currentRoom;
//         private boolean isPlayingWithBot;

//         public ClientHandler(Socket socket) {
//             this.socket = socket;
//         }

//         @Override
//         public void run() {
//             try {
//                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                 out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

//                 out.write("Enter your name: ");
//                 out.flush();
//                 playerName = in.readLine();
//                 System.out.println("Player " + playerName + " joined.");

//                 out.write("Choose game mode (1: Play with Bot, 2: Play with another Player): ");
//                 out.flush();
//                 String mode = in.readLine();
//                 System.out.println( playerName + " choose " + mode );

//                 if ("1".equals(mode)) {
//                     isPlayingWithBot = true;
//                     System.out.println("test1");
//                     playWithBot();
                    
//                 } else if ("2".equals(mode)) {
//                     isPlayingWithBot = false;
//                     System.out.println("test2");
//                     joinMultiplayerRoom();
                    
//                 } else {
//                     out.write("Invalid option. Disconnecting...\n");
//                     out.flush();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             } finally {
//                 closeConnection();
//             }
//         }

//         private void playWithBot() throws IOException {
//             while (true) {
//                 out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//                 out.flush();
//                 String playerMove = in.readLine().toUpperCase();

//                 if ("Q".equals(playerMove)) {
//                     out.write("You have quit the game.\n");
//                     out.flush();
//                     break;
//                 }

//                 String botMove = generateBotMove();
//                 String result = determineWinner(playerName, "Bot", playerMove, botMove);

//                 out.write("Result: " + result + "\n");
//                 out.flush();
//             }
//         }

//         private void joinMultiplayerRoom() throws IOException {
//             synchronized (roomLock) {
//                 for (Room room : rooms.values()) {
//                     if (!room.isFull()) {
//                         currentRoom = room;
//                         break;
//                     }
//                 }

//                 if (currentRoom == null) {
//                     currentRoom = new Room(++roomCounter);
//                     rooms.put(currentRoom.getId(), currentRoom);
//                 }

//                 currentRoom.addPlayer(this);

//                 if (currentRoom.isFull()) {
//                     startMultiplayerGame();
//                 } else {
//                     out.write("Waiting for another player to join...\n");
//                     out.flush();
//                 }
//             }
//         }

//         private void startMultiplayerGame() throws IOException {
//             List<ClientHandler> players = currentRoom.getPlayers();
//             ClientHandler player1 = players.get(0);
//             ClientHandler player2 = players.get(1);

//             player1.out.write("Another player joined. Starting the game...\n");
//             player1.out.flush();
//             player2.out.write("Another player joined. Starting the game...\n");
//             player2.out.flush();

//             while (true) {
//                 String player1Move = getPlayerMove(player1);
//                 if (player1Move == null) break;
//                 String player2Move = getPlayerMove(player2);
//                 if (player2Move == null) break;

//                 String result = determineWinner(player1.playerName, player2.playerName, player1Move, player2Move);

//                 player1.out.write("Result: " + result + "\n");
//                 player1.out.flush();
//                 player2.out.write("Result: " + result + "\n");
//                 player2.out.flush();
//             }

//             currentRoom.removePlayer(player1);
//             currentRoom.removePlayer(player2);
//         }

//         private String getPlayerMove(ClientHandler player) throws IOException {
//             player.out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//             player.out.flush();
//             String move = player.in.readLine().toUpperCase();

//             if ("Q".equals(move)) {
//                 player.out.write("You have quit the game.\n");
//                 player.out.flush();
//                 return null;
//             }

//             return move;
//         }

//         private String generateBotMove() {
//             String[] moves = {"ROCK", "PAPER", "SCISSORS"};
//             Random random = new Random();
//             return moves[random.nextInt(moves.length)];
//         }

//         private String determineWinner(String player1Name, String player2Name, String player1Move, String player2Move) {
//             if (player1Move.equals(player2Move)) {
//                 return "It's a tie!";
//             }

//             switch (player1Move) {
//                 case "ROCK":
//                     return player2Move.equals("SCISSORS") ? player1Name + " wins!" : player2Name + " wins!";
//                 case "PAPER":
//                     return player2Move.equals("ROCK") ? player1Name + " wins!" : player2Name + " wins!";
//                 case "SCISSORS":
//                     return player2Move.equals("PAPER") ? player1Name + " wins!" : player2Name + " wins!";
//                 default:
//                     return "Invalid move!";
//             }
//         }

//         private void closeConnection() {
//             try {
//                 if (currentRoom != null) {
//                     currentRoom.removePlayer(this);
//                 }
//                 if (in != null) {
//                     in.close();
//                 }
//                 if (out != null) {
//                     out.close();
//                 }
//                 if (socket != null) {
//                     socket.close();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }

// import java.io.*;
// import java.net.*;
// import java.util.ArrayList;
// import java.util.Random;
// import java.util.Scanner;

// public class RockPaperScissorsServer2 {
//     private static ArrayList<MultiplayerRoom> rooms = new ArrayList<>();

//     public static void main(String[] args) throws IOException {
//         System.out.print("Enter port number: ");
//         Scanner scan = new Scanner(System.in);
//         int portNumber = scan.nextInt();

//         ServerSocket serverSocket = new ServerSocket(portNumber);
//         System.out.println("Server started and listening to port " + portNumber);

//         while (true) {
//             Socket clientSocket = serverSocket.accept();
//             new Thread(new ClientHandler(clientSocket)).start();
//         }
//     }

//     private static class ClientHandler implements Runnable {
//         private Socket clientSocket;
//         private BufferedReader in;
//         private BufferedWriter out;

//         public ClientHandler(Socket socket) {
//             this.clientSocket = socket;
//         }

//         @Override
//         public void run() {
//             try {
//                 in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                 out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

//                 // out.write("Enter your name: ");
//                 // out.flush();
//                 String playerName = in.readLine();

//                 // out.write("Choose game mode (1: Play with Bot, 2: Play with another Player): ");
//                 // out.flush();
//                 String mode = in.readLine();

//                 if ("1".equals(mode)) {
//                     playWithBot(playerName);
//                 } else if ("2".equals(mode)) {
//                     joinMultiplayerGame(playerName);
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }

//         private void playWithBot(String playerName) throws IOException {
//             String[] choices = {"ROCK", "PAPER", "SCISSORS"};
//             Random random = new Random();

//             while (true) {
//                 // out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//                 // out.flush();
//                 String playerMove = in.readLine().toUpperCase();

//                 if ("Q".equalsIgnoreCase(playerMove)) {
//                     out.write("You have exited the game.\n");
//                     out.flush();
//                     break;
//                 }

//                 String botMove = choices[random.nextInt(choices.length)];
//                 String result = determineWinner(playerMove, botMove);
//                 result = result.replace("Player 1", playerName).replace("Player 2", "Bot");

//                 out.write("Bot chose: " + botMove + " Results: " + result + "\n");
//                 out.flush();
//             }
//         }

//         private void joinMultiplayerGame(String playerName) throws IOException {
//             MultiplayerRoom availableRoom = null;
//             for (MultiplayerRoom room : rooms) {
//                 if (room.isWaitingForPlayer()) {
//                     availableRoom = room;
//                     break;
//                 }
//             }

//             if (availableRoom == null) {
//                 availableRoom = new MultiplayerRoom();
//                 rooms.add(availableRoom);
//             }

//             availableRoom.addPlayer(clientSocket, playerName);
//         }

//         private String determineWinner(String move1, String move2) {
//             if (move1.equalsIgnoreCase(move2)) {
//                 return "It's a tie!";
//             } else if ((move1.equalsIgnoreCase("ROCK") && move2.equalsIgnoreCase("SCISSORS")) ||
//                        (move1.equalsIgnoreCase("PAPER") && move2.equalsIgnoreCase("ROCK")) ||
//                        (move1.equalsIgnoreCase("SCISSORS") && move2.equalsIgnoreCase("PAPER"))) {
//                 return "Player 1 wins!";
//             } else {
//                 return "Player 2 wins!";
//             }
//         }
//     }

//     private static class MultiplayerRoom {
//         private Socket player1Socket;
//         private String player1Name;
//         private Socket player2Socket;
//         private String player2Name;
//         private BufferedReader player1In;
//         private BufferedWriter player1Out;
//         private BufferedReader player2In;
//         private BufferedWriter player2Out;

//         public synchronized void addPlayer(Socket playerSocket, String playerName) throws IOException {
//             if (player1Socket == null) {
//                 player1Socket = playerSocket;
//                 player1Name = playerName;
//                 player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
//                 player1Out = new BufferedWriter(new OutputStreamWriter(player1Socket.getOutputStream()));
//                 player1Out.write("Waiting for another player to join...\n");
//                 player1Out.flush();
//             } else {
//                 player2Socket = playerSocket;
//                 player2Name = playerName;
//                 player2In = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
//                 player2Out = new BufferedWriter(new OutputStreamWriter(player2Socket.getOutputStream()));

//                 new Thread(this::startGame).start();
//             }
//         }

//         public boolean isWaitingForPlayer() {
//             return player2Socket == null;
//         }

//         private void startGame() {
//             try {
//                 player1Out.write("Another player has joined. Let's start!\n");
//                 player1Out.flush();
//                 player2Out.write("You have joined the game. Let's start!\n");
//                 player2Out.flush();

//                 while (true) {
//                     // player1Out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//                     // player1Out.flush();
//                     String player1Move = player1In.readLine().toUpperCase();
//                     // System.out.println(player1Move);
//                     // player1Out.write(player1Move);
//                     // player1Out.flush();

//                     // player2Out.write("Enter your move (ROCK, PAPER, SCISSORS) or Q to quit: ");
//                     // player2Out.flush();
//                     String player2Move = player2In.readLine().toUpperCase();
//                     // player2Out.write("test");
//                     // player2Out.flush();

//                     if ("Q".equalsIgnoreCase(player1Move) || "Q".equalsIgnoreCase(player2Move)) {
//                         player1Out.write("Game over.\n");
//                         player1Out.flush();
//                         player2Out.write("Game over.\n");
//                         player2Out.flush();
//                         break;
//                     }

//                     String result = determineWinner(player1Move, player2Move);
//                     result = result.replace("Player 1", player1Name).replace("Player 2", player2Name);

//                     player1Out.write(player2Name + " chose: " + player2Move + "\n" + result + "\n");
//                     player1Out.flush();
//                     player2Out.write(player1Name + " chose: " + player1Move + "\n" + result + "\n");
//                     player2Out.flush();
//                 }
//             } catch (IOException e) {
//                 e.printStackTrace();
//             } finally {
//                 try {
//                     if (player1Socket != null) player1Socket.close();
//                     if (player2Socket != null) player2Socket.close();
//                 } catch (IOException e) {
//                     e.printStackTrace();
//                 }
//             }
//         }

//         private String determineWinner(String move1, String move2) {
//             if (move1.equalsIgnoreCase(move2)) {
//                 return "It's a tie!";
//             } else if ((move1.equalsIgnoreCase("ROCK") && move2.equalsIgnoreCase("SCISSORS")) ||
//                        (move1.equalsIgnoreCase("PAPER") && move2.equalsIgnoreCase("ROCK")) ||
//                        (move1.equalsIgnoreCase("SCISSORS") && move2.equalsIgnoreCase("PAPER"))) {
//                 return "Player 1 wins!";
//             } else {
//                 return "Player 2 wins!";
//             }
//         }
//     }
// }
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class RockPaperScissorsServer2 {
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




