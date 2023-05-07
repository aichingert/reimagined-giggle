package at.htlleonding.mill.model;

import at.htlleonding.mill.model.helper.Position;
import at.htlleonding.mill.model.helper.Logic;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class Mill {
    public final int BOARD_SIZE = 3;
    private final int[][][] board;
    private int moveCounter;
    private GameState gameState = GameState.SET;
    private final Player playerOne;
    private final Player playerTwo;

    public Mill(Player playerOne, Player playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.board = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        this.moveCounter = 0;
    }

    public boolean setPiece(int color, Position position) {
        if (gameState != GameState.SET ||
                this.playerOne.getAmountOfPieces() == Player.MAX_PIECES &&
                this.playerTwo.getAmountOfPieces() == Player.MAX_PIECES ||
                color != 1 && color != 2) {
            return false;
        }

        if ((this.board[position.getZ()][position.getY()][position.getX()] != 0) ||
                (position.getX() == 1 && position.getY() == 1)) {
            return false;
        }

        this.board[position.getZ()][position.getY()][position.getX()] = color;

        if (this.playerOne.getColor() == color) {
            this.playerOne.setPiece();
        } else {
            this.playerTwo.setPiece();
        }

        return true;
    }

    public boolean movePiece(int color, Position from, Position to) {
        // Note: There is no need to check if the current position is the same color as the piece we want to move
        // because we can only select pieces that have our color
        if (from.equals(to)) {
            return false;
        }

        // 0 0 0
        // 0   0
        // 0 0 0

        List<Position> possibleMoves = Logic.getMoves(this, from);

        if (!possibleMoves.contains(to)) {
            return false;
        }

        board[from.getZ()][from.getY()][from.getX()] = 0;
        board[to.getZ()][to.getY()][to.getX()] = color;
        return true;
    }

    public int getValueAt(Position position) {
        return this.board[position.getZ()][position.getY()][position.getX()];
    }

    public int getCurrentPlayerColor() {
        if (this.playerOne.isPlayerTurn()) {
            return this.playerOne.getColor();
        }

        return this.playerTwo.getColor();
    }

    public void switchTurn() {
        this.moveCounter += 1;
        this.playerOne.setPlayerTurn(!this.playerOne.isPlayerTurn());
        this.playerTwo.setPlayerTurn(!this.playerTwo.isPlayerTurn());
    }

    public GameState getGameState() {
        return gameState;
    }

    public void updateGameState() {
        if (this.moveCounter < 2 * Player.MAX_PIECES) {
            return;
        }

        this.gameState = GameState.MOVE;
    }
}