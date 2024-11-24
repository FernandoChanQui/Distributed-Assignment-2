import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.List;

public class TriviaClient {
    private TriviaService triviaService;
    private String playerName;
    private Scanner scanner;

    public TriviaClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            // Connect to the RMI registry and get the TriviaService reference
            Registry registry = LocateRegistry.getRegistry("localhost", 6666);
            triviaService = (TriviaService) registry.lookup("TriviaService");
    
            // Prompt for the player's name
            System.out.println("Enter your name:");
            playerName = scanner.nextLine();
    
            // Register the player with the server
            String welcome = triviaService.registerPlayer(playerName);
            System.out.println(welcome);
    
            boolean playAgain = true;
            while (playAgain) {
                // Play the game
                playGame();
    
                // Ask if the player wants to play again
                System.out.println("Do you want to play again? (yes/no)");
                String userInput = scanner.nextLine();
    
                // If player answers "yes", they can play again; if "no", disconnect them
                playAgain = userInput.equalsIgnoreCase("yes");
    
                if (playAgain) {
                    triviaService.playAgain(playerName); // Reset the game for the player
                } else {
                    // If player chooses not to play again, disconnect them
                    System.out.println("Thank you for playing! Goodbye, " + playerName + "!");
                    triviaService.disconnectPlayer(playerName); // Notify the server that the player has left
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during the client-server interaction
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    

    private void playGame() throws Exception {
        String[] question;
        int questionNumber = 1;
    
        // Notification listener for new players and disconnections
        new Thread(() -> {
            try {
                while (true) {
                    String notification = triviaService.getNewPlayerNotification();
                    if (notification != null) {
                        System.out.println(notification);
                    }
                }
            } catch (Exception e) {
                System.err.println("Notification error: " + e.toString());
            }
        }).start();
    
        while ((question = triviaService.getQuestion(playerName)) != null) {
            System.out.println("\nQuestion #" + questionNumber + ": " + question[0]);
            String answer = scanner.nextLine();
    
            String[] feedback = triviaService.submitAnswerWithFeedback(playerName, answer);
            boolean isCorrect = Boolean.parseBoolean(feedback[0]);
    
            if (isCorrect) {
                System.out.println("Correct! Your current score: " + feedback[1]);
            } else {
                System.out.println("Wrong! The correct answer was: " + feedback[2]);
                System.out.println("Your current score: " + feedback[1]);
            }
            questionNumber++;
        }
    
        System.out.println("\nGame finished! Final scores:");
        displayLeaderboard();
    }

    private void displayLeaderboard() throws Exception {
        List<Player> leaderboard = triviaService.getLeaderboard();
        System.out.println("\n--- Leaderboard ---");
        for (Player player : leaderboard) {
            System.out.println(player.getName() + ": " + player.getScore() + " points");
        }
    }

    public static void main(String[] args) {
        TriviaClient client = new TriviaClient();
        client.start();
    }
}
