package at.htlleonding.mill.model;

public class Player {
    public static final int MAX_PIECES = 3;
    private int amountOfPieces;
    private final int color;
    private boolean isPlayerTurn;


    public Player(int color) {
        this.amountOfPieces = 0;
        this.color = color;
        this.isPlayerTurn = color == 1;
    }

    public int getColor() {
        return this.color;
    }

    public int getAmountOfPieces() {
        return this.amountOfPieces;
    }

    public void setPiece() {
        this.amountOfPieces += 1;
    }

    public boolean isPlayerTurn() {
        return this.isPlayerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.isPlayerTurn = playerTurn;
    }
}