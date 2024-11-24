import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class TriviaServiceImpl extends UnicastRemoteObject implements TriviaService {
    private static final long serialVersionUID = 1L;
    private Map<String, Player> players = new HashMap<>();
    private List<Player> highScoreTable = new ArrayList<>();
    private Map<String, Integer> currentQuestionIndex = new HashMap<>();
    private Map<String, String> currentCorrectAnswer = new HashMap<>();
    private Map<String, Integer> highScores = new HashMap<>();
    private static final int TOTAL_QUESTIONS = 10;
    private static final int POINTS_PER_QUESTION = 10;
    private Queue<String> newPlayerNotificationQueue = new LinkedList<>();

    

    private static final String[][] easyQuestions = {
        {"What is the capital of Canada?", "Ottawa"},
        {"How many planets are in our solar system?", "Eight"},
        {"Who painted the Mona Lisa?", "Leonardo da Vinci"},
        {"What is the largest ocean on Earth?", "Pacific Ocean"},
        {"What is the chemical symbol for gold?", "Au"},
        {"Who wrote the play 'Romeo and Juliet'?", "William Shakespeare"},
        {"What is the square root of 144?", "12"},
        {"What is the opposite of hot?", "Cold"},
        {"How many sides does a triangle have?", "Three"},
        {"What is the largest country in the world by land area?", "Russia"}
    };

    private static final String[][] mediumQuestions = {
        {"What is the longest river in North America?", "The Mississippi River"}, 
        {"Who was the first president of the United States?", "George Washington"}, 
        {"What is the chemical symbol for gold?", "Au"}, 
        {"Who wrote the famous novel Pride and Prejudice?", "Jane Austen"}, 
        {"What is the square root of 169?", "13"}, 
        {"Who painted the Mona Lisa?", "Leonardo da Vinci"}, 
        {"What is the most famous musical composition by Ludwig van Beethoven?", "Symphony No. 9"}, 
        {"What is the philosophical concept of cogito ergo sum often associated with?", "René Descartes"}, 
        {"Who is considered the father of psychoanalysis?", "Sigmund Freud"}, 
        {"In which sport is the term ace used?", "Tennis"}
    };

    private static final String[][] hardQuestions = {
        {"What is the largest freshwater lake in the world?", "Lake Superior"}, 
        {"Who was the first person to walk on the moon?", "Neil Armstrong"}, 
        {"What is the smallest unit of matter?", "Atom"}, 
        {"Who wrote the play Hamlet?", "William Shakespeare"}, 
        {"What is the value of pi to 10 decimal places?", "3.1415926536"}, 
        {"Who is the composer of the opera The Magic Flute?", "Wolfgang Amadeus Mozart"}, 
        {"What is the famous theory proposed by Albert Einstein?", "Theory of relativity"}, 
        {"Who is the founder of modern psychology?", "Wilhelm Wundt"}, 
        {"What is the term for the study of the human mind and behavior?", "Psychology"}, 
        {"What is the name of the largest muscle in the human body?", "Gluteus Maximus"}
    };
    
    public TriviaServiceImpl() throws RemoteException {
        super();
    }

    @Override
public String registerPlayer(String name) throws RemoteException {
    if (players.containsKey(name)) {
        return "Player with this name already exists!";
    }

    Player player = new Player(name);
    players.put(name, player);

    // Initialize player data
    currentQuestionIndex.put(name, 0);
    currentCorrectAnswer.put(name, null);
    highScores.putIfAbsent(name, 0);

    // Announce the new player
    System.out.println("New player entered: " + name);
    newPlayerNotificationQueue.add("Player " + name + " has entered the game!");

    return "Welcome to the trivia quiz, " + name + "!";
}

    // Rest of the class remains the same...
    @Override
    public String[] getQuestion(String playerName) throws RemoteException {
        Player player = players.get(playerName);
        if (player == null) {
            throw new RemoteException("Player not found");
        }

        Integer index = currentQuestionIndex.get(playerName);
        if (index == null) {
            throw new RemoteException("Player's question index is not initialized");
        }
        
        if (index >= TOTAL_QUESTIONS) {
            return null;  // No more questions left
        }

        String[] question = getQuestionForDifficulty(player.getScore());
        currentQuestionIndex.put(playerName, index + 1);
        currentCorrectAnswer.put(playerName, question[1]);

        return new String[]{question[0]};
    }

    @Override
    public int submitAnswer(String playerName, String answer) throws RemoteException {
        Player player = players.get(playerName);
        if (player == null) {
            throw new RemoteException("Player not found");
        }

        String correctAnswer = currentCorrectAnswer.get(playerName);
        if (correctAnswer == null) {
            throw new RemoteException("No current question found for player");
        }

        if (answer.trim().equalsIgnoreCase(correctAnswer.trim())) {
            player.addScore(POINTS_PER_QUESTION);
        }

        currentCorrectAnswer.remove(playerName);

        if (currentQuestionIndex.get(playerName) == TOTAL_QUESTIONS) {
            int currentHighScore = highScores.get(playerName);
            if (player.getScore() > currentHighScore) {
                highScores.put(playerName, player.getScore());
            }
            updateHighScoreTable(player);
        }

        return player.getScore();
    }

    private void updateHighScoreTable(Player player) {
        highScoreTable.removeIf(p -> p.getName().equals(player.getName()));
        Player highScorer = new Player(player.getName());
        highScorer.setScore(highScores.get(player.getName()));
        highScoreTable.add(highScorer);
        highScoreTable.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
    }

    @Override
    public List<Player> getLeaderboard() throws RemoteException {
        return new ArrayList<>(highScoreTable);
    }

    @Override
    public boolean playAgain(String playerName) throws RemoteException {
        Player player = players.get(playerName);
        if (player == null) {
            throw new RemoteException("Player not found");
        }

        player.resetScore();
        currentQuestionIndex.put(playerName, 0);
        currentCorrectAnswer.remove(playerName);
        return true;
    }

    private String[] getQuestionForDifficulty(int score) {
        if (score >= 60) {
            return hardQuestions[new Random().nextInt(hardQuestions.length)];
        } else if (score >= 30) {
            return mediumQuestions[new Random().nextInt(mediumQuestions.length)];
        } else {
            return easyQuestions[new Random().nextInt(easyQuestions.length)];
        }
    }

    @Override
public synchronized void disconnectPlayer(String playerName) throws RemoteException {
    if (players.containsKey(playerName)) {
        // Remove player from all data structures
        players.remove(playerName);
        currentQuestionIndex.remove(playerName);
        currentCorrectAnswer.remove(playerName);
        highScores.remove(playerName);

        // Log the disconnection on the server
        System.out.println("Player disconnected: " + playerName);

        // Notify other players about the disconnection
        newPlayerNotificationQueue.add("Player " + playerName + " has left the game!");
    }
}


    @Override
public String[] submitAnswerWithFeedback(String playerName, String answer) throws RemoteException {
    Player player = players.get(playerName);
    if (player == null) {
        throw new RemoteException("Player not found");
    }

    String correctAnswer = currentCorrectAnswer.get(playerName);
    if (correctAnswer == null) {
        throw new RemoteException("No current question found for player");
    }

    boolean isCorrect = answer.trim().equalsIgnoreCase(correctAnswer.trim());
    if (isCorrect) {
        player.addScore(POINTS_PER_QUESTION);
    }

    currentCorrectAnswer.remove(playerName);

    if (currentQuestionIndex.get(playerName) == TOTAL_QUESTIONS) {
        int currentHighScore = highScores.get(playerName);
        if (player.getScore() > currentHighScore) {
            highScores.put(playerName, player.getScore());
        }
        updateHighScoreTable(player);
    }

    return new String[]{
        String.valueOf(isCorrect),
        String.valueOf(player.getScore()),
        correctAnswer
    };
}

@Override
public String getNewPlayerNotification() throws RemoteException {
    return newPlayerNotificationQueue.poll(); // Returns the next notification or null if empty
}

}