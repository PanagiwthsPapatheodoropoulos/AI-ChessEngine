public class ChessPiece {
    public enum Type { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING };

    private Type type;
    private ChessBoard.Player player;

    public ChessPiece(Type type, ChessBoard.Player player) {
        this.type = type;
        this.player = player;
    }

    public Type getType() {
        return type;
    }

    public ChessBoard.Player getPlayer() {
        return player;
    }

    public String getSymbol() {
        switch (type) {
            case PAWN: return player == ChessBoard.Player.WHITE ? "♙" : "♟";
            case ROOK: return player == ChessBoard.Player.WHITE ? "♖" : "♜";
            case KNIGHT: return player == ChessBoard.Player.WHITE ? "♘" : "♞";
            case BISHOP: return player == ChessBoard.Player.WHITE ? "♗" : "♝";
            case QUEEN: return player == ChessBoard.Player.WHITE ? "♕" : "♛";
            case KING: return player == ChessBoard.Player.WHITE ? "♔" : "♚";
            default: return "";
        }
    }
}

