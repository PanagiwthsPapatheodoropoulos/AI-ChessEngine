import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChessAI {
    private static final int MAX_DEPTH = 4;
    private Random random = new Random();

    public Move getBestMove(ChessBoard board) {
        List<Move> possibleMoves = board.getAllValidMoves();
        if (possibleMoves.isEmpty()) {
            return null; // No valid moves, game is over
        }

        Collections.shuffle(possibleMoves); // Shuffle moves for variety
        
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        ChessBoard.Player currentPlayer = board.getCurrentPlayer();
        
        for (Move move : possibleMoves) {
            ChessBoard newBoard = board.copy();
            newBoard.makeMove(move);
            int value = minimax(newBoard, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, currentPlayer != ChessBoard.Player.WHITE);
            
            // Add some randomness to prevent repetitive play
            value += random.nextInt(10) - 5;
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int minimax(ChessBoard board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || board.isGameOver()) {
            return evaluateBoard(board);
        }

        List<Move> possibleMoves = board.getAllValidMoves();
        Collections.shuffle(possibleMoves); // Shuffle moves for variety

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                ChessBoard newBoard = board.copy();
                newBoard.makeMove(move);
                int eval = minimax(newBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : possibleMoves) {
                ChessBoard newBoard = board.copy();
                newBoard.makeMove(move);
                int eval = minimax(newBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private int evaluateBoard(ChessBoard board) {
        if (board.isCheckmate(ChessBoard.Player.WHITE)) {
            return -10000;
        } else if (board.isCheckmate(ChessBoard.Player.BLACK)) {
            return 10000;
        } else if (board.isStalemate(board.getCurrentPlayer()) || board.isDraw()) {
            return 0;
        }

        int score = 0;
        int whiteMaterial = 0;
        int blackMaterial = 0;
        int whitePositionScore = 0;
        int blackPositionScore = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    int pieceValue = getPieceValue(piece.getType());
                    int positionValue = getPositionValue(piece.getType(), row, col, piece.getPlayer());
                    
                    if (piece.getPlayer() == ChessBoard.Player.WHITE) {
                        whiteMaterial += pieceValue;
                        whitePositionScore += positionValue;
                    } else {
                        blackMaterial += pieceValue;
                        blackPositionScore += positionValue;
                    }
                }
            }
        }

        score = (whiteMaterial - blackMaterial) + (whitePositionScore - blackPositionScore);

        // Evaluate pawn structure
        score += evaluatePawnStructure(board);

        // Evaluate piece activity
        score += evaluatePieceActivity(board);

        // Evaluate king safety
        score += evaluateKingSafety(board);

        return score;
    }

    private int getPieceValue(ChessPiece.Type type) {
        switch (type) {
            case PAWN: return 100;
            case KNIGHT: return 320;
            case BISHOP: return 330;
            case ROOK: return 500;
            case QUEEN: return 900;
            case KING: return 20000;
            default: return 0;
        }
    }

    private int getPositionValue(ChessPiece.Type type, int row, int col, ChessBoard.Player player) {
        int[][] pawnTable = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            { 5,  5, 10, 25, 25, 10,  5,  5},
            { 0,  0,  0, 20, 20,  0,  0,  0},
            { 5, -5,-10,  0,  0,-10, -5,  5},
            { 5, 10, 10,-20,-20, 10, 10,  5},
            { 0,  0,  0,  0,  0,  0,  0,  0}
        };

        int[][] knightTable = {
            {-50,-40,-30,-30,-30,-30,-40,-50},
            {-40,-20,  0,  0,  0,  0,-20,-40},
            {-30,  0, 10, 15, 15, 10,  0,-30},
            {-30,  5, 15, 20, 20, 15,  5,-30},
            {-30,  0, 15, 20, 20, 15,  0,-30},
            {-30,  5, 10, 15, 15, 10,  5,-30},
            {-40,-20,  0,  5,  5,  0,-20,-40},
            {-50,-40,-30,-30,-30,-30,-40,-50}
        };

        int[][] bishopTable = {
            {-20,-10,-10,-10,-10,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5, 10, 10,  5,  0,-10},
            {-10,  5,  5, 10, 10,  5,  5,-10},
            {-10,  0, 10, 10, 10, 10,  0,-10},
            {-10, 10, 10, 10, 10, 10, 10,-10},
            {-10,  5,  0,  0,  0,  0,  5,-10},
            {-20,-10,-10,-10,-10,-10,-10,-20}
        };

        int[][] rookTable = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            { 5, 10, 10, 10, 10, 10, 10,  5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            { 0,  0,  0,  5,  5,  0,  0,  0}
        };

        int[][] queenTable = {
            {-20,-10,-10, -5, -5,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5,  5,  5,  5,  0,-10},
            { -5,  0,  5,  5,  5,  5,  0, -5},
            {  0,  0,  5,  5,  5,  5,  0, -5},
            {-10,  5,  5,  5,  5,  5,  0,-10},
            {-10,  0,  5,  0,  0,  0,  0,-10},
            {-20,-10,-10, -5, -5,-10,-10,-20}
        };

        int[][] kingTable = {
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-20,-30,-30,-40,-40,-30,-30,-20},
            {-10,-20,-20,-20,-20,-20,-20,-10},
            { 20, 20,  0,  0,  0,  0, 20, 20},
            { 20, 30, 10,  0,  0, 10, 30, 20}
        };

        int[][] selectedTable;
        switch (type) {
            case PAWN: selectedTable = pawnTable; break;
            case KNIGHT: selectedTable = knightTable; break;
            case BISHOP: selectedTable = bishopTable; break;
            case ROOK: selectedTable = rookTable; break;
            case QUEEN: selectedTable = queenTable; break;
            case KING: selectedTable = kingTable; break;
            default: return 0;
        }

        if (player == ChessBoard.Player.WHITE) {
            return selectedTable[7 - row][col];
        } else {
            return selectedTable[row][col];
        }
    }

    private int evaluatePawnStructure(ChessBoard board) {
        int score = 0;
        for (int col = 0; col < 8; col++) {
            boolean whitePawnInColumn = false;
            boolean blackPawnInColumn = false;
            for (int row = 0; row < 8; row++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.getType() == ChessPiece.Type.PAWN) {
                    if (piece.getPlayer() == ChessBoard.Player.WHITE) {
                        whitePawnInColumn = true;
                    } else {
                        blackPawnInColumn = true;
                    }
                }
            }
            if (whitePawnInColumn) score -= 10; // Penalize doubled pawns
            if (blackPawnInColumn) score += 10;
            if (!whitePawnInColumn) score -= 20; // Penalize open files
            if (!blackPawnInColumn) score += 20;
        }
        return score;
    }

    private int evaluatePieceActivity(ChessBoard board) {
        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    final int currentRow = row; 
                    final int currentCol = col;
                    int mobility = (int) board.getAllValidMoves().stream()
                        .filter(m -> m.getFromRow() == currentRow && m.getFromCol() == currentCol)
                        .count();
                
                    if (piece.getPlayer() == ChessBoard.Player.WHITE) {
                        score += mobility * 5;
                    } else {
                        score -= mobility * 5;
                    }
                }
            }
        }
        return score;
    }

    private int evaluateKingSafety(ChessBoard board) {
        int score = 0;
        int[] whiteKingPos = findKing(board, ChessBoard.Player.WHITE);
        int[] blackKingPos = findKing(board, ChessBoard.Player.BLACK);

        if (whiteKingPos != null) {
            score -= evaluateKingSafetyForPlayer(board, whiteKingPos[0], whiteKingPos[1], ChessBoard.Player.WHITE);
        }
        if (blackKingPos != null) {
            score += evaluateKingSafetyForPlayer(board, blackKingPos[0], blackKingPos[1], ChessBoard.Player.BLACK);
        }

        return score;
    }

    private int evaluateKingSafetyForPlayer(ChessBoard board, int kingRow, int kingCol, ChessBoard.Player player) {
        int safety = 0;
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        
        for (int[] dir : directions) {
            int newRow = kingRow + dir[0];
            int newCol = kingCol + dir[1];
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                ChessPiece piece = board.getPiece(newRow, newCol);
                if (piece != null && piece.getPlayer() == player) {
                    safety += 10; // Friendly piece protecting the king
                }
            }
        }
        
        // Penalize for open files near the king
        for (int col = Math.max(0, kingCol - 1); col <= Math.min(7, kingCol + 1); col++) {
            boolean openFile = true;
            for (int row = 0; row < 8; row++) {
                if (board.getPiece(row, col) != null) {
                    openFile = false;
                    break;
                }
            }
            if (openFile) {
                safety -= 20;
            }
        }
        
        return safety;
    }

    private int[] findKing(ChessBoard board, ChessBoard.Player player) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.getType() == ChessPiece.Type.KING && piece.getPlayer() == player) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }
}

