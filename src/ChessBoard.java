import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    private ChessPiece[][] board;
    private Player currentPlayer;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int moveCount;
    private int halfMoveClock; // For fifty-move rule
    private List<Move> moveHistory = new ArrayList<>();
    private List<ChessPiece> capturedPieces = new ArrayList<>();

    public enum Player { WHITE, BLACK }

    public ChessBoard() {
        board = new ChessPiece[8][8];
        resetBoard();
    }

    public void resetBoard() {
        // Initialize white pieces
        board[0][0] = new ChessPiece(ChessPiece.Type.ROOK, Player.WHITE);
        board[0][1] = new ChessPiece(ChessPiece.Type.KNIGHT, Player.WHITE);
        board[0][2] = new ChessPiece(ChessPiece.Type.BISHOP, Player.WHITE);
        board[0][3] = new ChessPiece(ChessPiece.Type.QUEEN, Player.WHITE);
        board[0][4] = new ChessPiece(ChessPiece.Type.KING, Player.WHITE);
        board[0][5] = new ChessPiece(ChessPiece.Type.BISHOP, Player.WHITE);
        board[0][6] = new ChessPiece(ChessPiece.Type.KNIGHT, Player.WHITE);
        board[0][7] = new ChessPiece(ChessPiece.Type.ROOK, Player.WHITE);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new ChessPiece(ChessPiece.Type.PAWN, Player.WHITE);
        }

        // Initialize black pieces
        board[7][0] = new ChessPiece(ChessPiece.Type.ROOK, Player.BLACK);
        board[7][1] = new ChessPiece(ChessPiece.Type.KNIGHT, Player.BLACK);
        board[7][2] = new ChessPiece(ChessPiece.Type.BISHOP, Player.BLACK);
        board[7][3] = new ChessPiece(ChessPiece.Type.QUEEN, Player.BLACK);
        board[7][4] = new ChessPiece(ChessPiece.Type.KING, Player.BLACK);
        board[7][5] = new ChessPiece(ChessPiece.Type.BISHOP, Player.BLACK);
        board[7][6] = new ChessPiece(ChessPiece.Type.KNIGHT, Player.BLACK);
        board[7][7] = new ChessPiece(ChessPiece.Type.ROOK, Player.BLACK);
        for (int i = 0; i < 8; i++) {
            board[6][i] = new ChessPiece(ChessPiece.Type.PAWN, Player.BLACK);
        }

        // Initialize empty squares
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }

        currentPlayer = Player.WHITE;
        moveCount = 1;
        halfMoveClock = 0;
    }

    public ChessPiece getPiece(int row, int col) {
        return board[row][col];
    }

    public boolean selectSquare(int row, int col) {
        if (selectedRow == -1) {
            if (board[row][col] != null && board[row][col].getPlayer() == currentPlayer) {
                selectedRow = row;
                selectedCol = col;
                return true;
            }
        } else {
            Move move = new Move(selectedRow, selectedCol, row, col);
            if (isValidMove(move)) {
                makeMove(move);
                selectedRow = -1;
                selectedCol = -1;
                return true;
            } else {
                selectedRow = -1;
                selectedCol = -1;
            }
        }
        return false;
    }

    public void makeMove(Move move) {
        ChessPiece capturedPiece = board[move.getToRow()][move.getToCol()];
        ChessPiece movingPiece = board[move.getFromRow()][move.getFromCol()];
        
        board[move.getToRow()][move.getToCol()] = movingPiece;
        board[move.getFromRow()][move.getFromCol()] = null;
        
        moveHistory.add(move);
        capturedPieces.add(capturedPiece);
        
        if (currentPlayer == Player.BLACK) {
            moveCount++;
        }
        
        if (movingPiece.getType() == ChessPiece.Type.PAWN || capturedPiece != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        currentPlayer = (currentPlayer == Player.WHITE) ? Player.BLACK : Player.WHITE;
    }

    public boolean isValidMove(Move move) {
        ChessPiece piece = board[move.getFromRow()][move.getFromCol()];
        if (piece == null || piece.getPlayer() != currentPlayer) {
            return false;
        }

        // Check if the destination square is occupied by a piece of the same color
        if (board[move.getToRow()][move.getToCol()] != null && 
            board[move.getToRow()][move.getToCol()].getPlayer() == currentPlayer) {
            return false;
        }

        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToCol() - move.getFromCol());

        boolean isValidPieceMove = false;

        switch (piece.getType()) {
            case PAWN:
                isValidPieceMove = isValidPawnMove(move);
                break;
            case ROOK:
                isValidPieceMove = (rowDiff == 0 || colDiff == 0) && isClearPath(move);
                break;
            case KNIGHT:
                isValidPieceMove = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
                break;
            case BISHOP:
                isValidPieceMove = rowDiff == colDiff && isClearPath(move);
                break;
            case QUEEN:
                isValidPieceMove = (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && isClearPath(move);
                break;
            case KING:
                isValidPieceMove = rowDiff <= 1 && colDiff <= 1;
                break;
        }

        if (!isValidPieceMove) {
            return false;
        }

        // Check if the move would leave the king in check
        ChessBoard tempBoard = this.copy();
        tempBoard.makeMove(move);
        return !tempBoard.isKingInCheck(currentPlayer);
    }

    private boolean isValidPawnMove(Move move) {
        int direction = (currentPlayer == Player.WHITE) ? 1 : -1;
        int startRow = (currentPlayer == Player.WHITE) ? 1 : 6;

        // Moving forward
        if (move.getFromCol() == move.getToCol()) {
            if (move.getToRow() == move.getFromRow() + direction && board[move.getToRow()][move.getToCol()] == null) {
                return true;
            }
            if (move.getFromRow() == startRow && move.getToRow() == move.getFromRow() + 2 * direction &&
                board[move.getFromRow() + direction][move.getToCol()] == null &&
                board[move.getToRow()][move.getToCol()] == null) {
                return true;
            }
        }
        // Capturing diagonally
        else if (Math.abs(move.getToCol() - move.getFromCol()) == 1 && move.getToRow() == move.getFromRow() + direction) {
            return board[move.getToRow()][move.getToCol()] != null &&
                   board[move.getToRow()][move.getToCol()].getPlayer() != currentPlayer;
        }
        return false;
    }

    private boolean isClearPath(Move move) {
        int rowStep = Integer.compare(move.getToRow(), move.getFromRow());
        int colStep = Integer.compare(move.getToCol(), move.getFromCol());
        int row = move.getFromRow() + rowStep;
        int col = move.getFromCol() + colStep;

        while (row != move.getToRow() || col != move.getToCol()) {
            if (board[row][col] != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }

        return true;
    }

    public List<Move> getAllValidMoves() {
        List<Move> validMoves = new ArrayList<>();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                if (board[fromRow][fromCol] != null && board[fromRow][fromCol].getPlayer() == currentPlayer) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            Move move = new Move(fromRow, fromCol, toRow, toCol);
                            if (isValidMove(move)) {
                                validMoves.add(move);
                            }
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    public boolean isGameOver() {
        return isCheckmate(currentPlayer) || isStalemate(currentPlayer) || isDraw();
    }

    public boolean isCheckmate(Player player) {
        return isKingInCheck(player) && getAllValidMoves().isEmpty();
    }

    public boolean isStalemate(Player player) {
        return !isKingInCheck(player) && getAllValidMoves().isEmpty();
    }

    public boolean isDraw() {
        return isInsufficientMaterial() || isFiftyMoveDraw() || isThreefoldRepetition();
    }

    private boolean isInsufficientMaterial() {
        int whiteBishops = 0, whiteKnights = 0, blackBishops = 0, blackKnights = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    switch (piece.getType()) {
                        case PAWN:
                        case ROOK:
                        case QUEEN:
                            return false;
                        case BISHOP:
                            if (piece.getPlayer() == Player.WHITE) whiteBishops++;
                            else blackBishops++;
                            break;
                        case KNIGHT:
                            if (piece.getPlayer() == Player.WHITE) whiteKnights++;
                            else blackKnights++;
                            break;
                    }
                }
            }
        }
        return (whiteBishops + whiteKnights <= 1) && (blackBishops + blackKnights <= 1);
    }

    private boolean isFiftyMoveDraw() {
        return halfMoveClock >= 100; // 50 full moves (100 half-moves)
    }

    // This is a simplified version. A full implementation would require storing board states.
    private boolean isThreefoldRepetition() {
        // Implement if needed
        return false;
    }

    public boolean isKingInCheck(Player player) {
        int[] kingPosition = findKing(player);
        if (kingPosition == null) {
            return false; // This shouldn't happen in a valid game state
        }

        Player opponent = (player == Player.WHITE) ? Player.BLACK : Player.WHITE;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.getPlayer() == opponent) {
                    Move move = new Move(row, col, kingPosition[0], kingPosition[1]);
                    if (isValidMove(move)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int[] findKing(Player player) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.getType() == ChessPiece.Type.KING && piece.getPlayer() == player) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public ChessBoard copy() {
        ChessBoard newBoard = new ChessBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (this.board[i][j] != null) {
                    newBoard.board[i][j] = new ChessPiece(this.board[i][j].getType(), this.board[i][j].getPlayer());
                } else {
                    newBoard.board[i][j] = null;
                }
            }
        }
        newBoard.currentPlayer = this.currentPlayer;
        newBoard.moveCount = this.moveCount;
        newBoard.halfMoveClock = this.halfMoveClock;
        return newBoard;
    }

    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }
        
        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        ChessPiece capturedPiece = capturedPieces.remove(capturedPieces.size() - 1);
        
        ChessPiece movingPiece = board[lastMove.getToRow()][lastMove.getToCol()];
        board[lastMove.getFromRow()][lastMove.getFromCol()] = movingPiece;
        board[lastMove.getToRow()][lastMove.getToCol()] = capturedPiece;
        
        if (currentPlayer == Player.WHITE) {
            moveCount--;
        }
        
        currentPlayer = (currentPlayer == Player.WHITE) ? Player.BLACK : Player.WHITE;
        
        // Adjust halfMoveClock if necessary
        if (movingPiece.getType() == ChessPiece.Type.PAWN || capturedPiece != null) {
            halfMoveClock++;
        } else {
            halfMoveClock--;
        }
        
        return true;
    }
}

