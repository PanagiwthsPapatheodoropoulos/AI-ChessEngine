import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ChessGUI {
    private Stage stage;
    private ChessBoard chessBoard;
    private ChessAI chessAI;
    private GridPane boardGrid;
    private Label statusLabel;
    private Button aiMoveButton;
    private Button undoButton;


    public ChessGUI(Stage stage) {
        this.stage = stage;
        this.chessBoard = new ChessBoard();
        this.chessAI = new ChessAI();
        createUI();
    }

    private void createUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("AI Chess Engine");
        titleLabel.setFont(Font.font(24));

        boardGrid = new GridPane();
        updateBoardUI();

        statusLabel = new Label("White's turn");

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> newGame());

        aiMoveButton = new Button("AI Move");
        aiMoveButton.setOnAction(e -> makeAIMove());

        undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undoMove());

        HBox buttonBox = new HBox(10, newGameButton, aiMoveButton, undoButton);

        root.getChildren().addAll(titleLabel, boardGrid, statusLabel, buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("AI Chess Engine");
    }

    private void updateBoardUI() {
        boardGrid.getChildren().clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createSquare(row, col);
                boardGrid.add(square, col, row);
            }
        }
    }

    private StackPane createSquare(int row, int col) {
        StackPane square = new StackPane();
        Color color = (row + col) % 2 == 0 ? Color.WHITE : Color.LIGHTGRAY;
        square.getChildren().add(new Rectangle(50, 50, color));

        ChessPiece piece = chessBoard.getPiece(row, col);
        if (piece != null) {
            Label pieceLabel = new Label(piece.getSymbol());
            pieceLabel.setFont(Font.font(20));
            square.getChildren().add(pieceLabel);
        }

        square.setOnMouseClicked(e -> handleSquareClick(row, col));

        return square;
    }

    private void handleSquareClick(int row, int col) {
        if (chessBoard.selectSquare(row, col)) {
            updateBoardUI();
            if (chessBoard.isGameOver()) {
                showGameOverDialog();
            } else if (chessBoard.getCurrentPlayer() == ChessBoard.Player.BLACK) {
                makeAIMove();
            }
        }
        updateStatusLabel();
    }

    private void makeAIMove() {
        aiMoveButton.setDisable(true);
        undoButton.setDisable(true);
        statusLabel.setText("AI is thinking...");

        Task<Move> aiTask = new Task<>() {
            @Override
            protected Move call() {
                return chessAI.getBestMove(chessBoard);
            }
        };

        aiTask.setOnSucceeded(event -> {
            Move aiMove = aiTask.getValue();
            chessBoard.makeMove(aiMove);
            updateBoardUI();
            if (chessBoard.isGameOver()) {
                showGameOverDialog();
            }
            updateStatusLabel();
            aiMoveButton.setDisable(false);
            undoButton.setDisable(false);
        });

        new Thread(aiTask).start();
    }

    private void newGame() {
        chessBoard.resetBoard();
        updateBoardUI();
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        String playerTurn = chessBoard.getCurrentPlayer() == ChessBoard.Player.WHITE ? "White" : "Black";
        statusLabel.setText(playerTurn + "'s turn");
    }

    private void showGameOverDialog() {
        String winner = chessBoard.getCurrentPlayer() == ChessBoard.Player.WHITE ? "Black" : "White";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Game Over! " + winner + " wins!");
        alert.showAndWait();
    }

    private void undoMove() {
        if (chessBoard.undoLastMove()) {
            updateBoardUI();
            updateStatusLabel();
        }
    }

    public void show() {
        stage.show();
    }
}

