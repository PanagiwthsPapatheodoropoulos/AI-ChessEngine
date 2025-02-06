import javafx.application.Application;
import javafx.stage.Stage;

public class AIChessEngine extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ChessGUI gui = new ChessGUI(primaryStage);
        gui.show();
    }
}

