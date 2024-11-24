import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TriviaService extends Remote {
    String registerPlayer(String name) throws RemoteException;
    String[] getQuestion(String playerName) throws RemoteException;
    int submitAnswer(String playerName, String answer) throws RemoteException;
    List<Player> getLeaderboard() throws RemoteException;
    boolean playAgain(String playerName) throws RemoteException;
    String[] submitAnswerWithFeedback(String playerName, String answer) throws RemoteException;
    String getNewPlayerNotification() throws RemoteException;
    void disconnectPlayer(String playerName) throws RemoteException;


}