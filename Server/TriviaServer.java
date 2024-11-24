import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class TriviaServer {
    public static void main(String[] args) {
        try {
            TriviaService triviaService = new TriviaServiceImpl();
            Registry registry = LocateRegistry.createRegistry(6666);
            registry.rebind("TriviaService", triviaService);
            System.out.println("Trivia server started...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}