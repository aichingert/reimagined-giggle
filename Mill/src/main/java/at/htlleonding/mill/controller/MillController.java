package at.htlleonding.mill.controller;

import at.htlleonding.mill.model.*;
import at.htlleonding.mill.model.helper.CurrentGame;
import at.htlleonding.mill.model.helper.Logic;
import at.htlleonding.mill.model.helper.Position;
import at.htlleonding.mill.repositories.GameRepository;
import at.htlleonding.mill.repositories.UserRepository;
import at.htlleonding.mill.view.GameBoard;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static at.htlleonding.mill.App.loadFXML;

public class MillController {
    Mill game;

    @FXML
    private GameBoard gameBoard;
    @FXML
    private Label lblPlayer1;
    @FXML
    private Label lblPlayer2;
    @FXML
    private Label lblPieces1;
    @FXML
    private Label lblPieces2;
    @FXML
    private Label lblPhase;

    private Circle currentlySelected;
    private List<Position> takeablePieces;
    private List<Move> movesForReplay;
    private Player playerOne;
    private Player playerTwo;
    boolean playerOneIsWhite;

    @FXML
    private void initialize() {
        UserRepository userRepository = new UserRepository();
        String player1Name = userRepository.findById(CurrentGame.getInstance().getPlayer1Id()).getAlias();
        String player2Name = userRepository.findById(CurrentGame.getInstance().getPlayer2Id()).getAlias();

        playerOneIsWhite = Math.random() > 0.5;
        this.playerOne = new Player(playerOneIsWhite ? 1 : 2);
        this.playerTwo = new Player(playerOneIsWhite ? 2 : 1);

        if (playerOneIsWhite) {
            lblPlayer1.setText(player1Name);
            lblPlayer2.setText(player2Name);

            this.playerOne.amountOfPiecesProperty().addListener((observableValue, number, t1) -> {
                lblPieces1.setText("Pieces on board: " + observableValue.getValue().toString());
            });
            this.playerTwo.amountOfPiecesProperty().addListener((observableValue, number, t1) -> {
                lblPieces2.setText("Pieces on board: " + observableValue.getValue().toString());
            });

            this.playerOne.isPlayerTurnProperty().addListener((observableValue, aBoolean, t1) -> {
                lblPlayer1.setDisable(!observableValue.getValue());
                lblPieces1.setDisable(!observableValue.getValue());
            });
            this.playerTwo.isPlayerTurnProperty().addListener((observableValue, aBoolean, t1) -> {
                lblPlayer2.setDisable(!observableValue.getValue());
                lblPieces2.setDisable(!observableValue.getValue());
            });
        }
        else {
            lblPlayer1.setText(player2Name);
            lblPlayer2.setText(player1Name);

            this.playerTwo.amountOfPiecesProperty().addListener((observableValue, number, t1) -> {
                lblPieces1.setText("Pieces on board: " + observableValue.getValue().toString());
            });
            this.playerOne.amountOfPiecesProperty().addListener((observableValue, number, t1) -> {
                lblPieces2.setText("Pieces on board: " + observableValue.getValue().toString());
            });

            this.playerOne.isPlayerTurnProperty().addListener((observableValue, aBoolean, t1) -> {
                lblPlayer2.setDisable(!observableValue.getValue());
                lblPieces2.setDisable(!observableValue.getValue());
            });
            this.playerTwo.isPlayerTurnProperty().addListener((observableValue, aBoolean, t1) -> {
                lblPlayer1.setDisable(!observableValue.getValue());
                lblPieces1.setDisable(!observableValue.getValue());
            });
        }

        this.game = new Mill(playerOne, playerTwo);
        this.currentlySelected = null;
        this.takeablePieces = null;
        this.movesForReplay = new ArrayList<>();

        lblPlayer2.setDisable(true);
        lblPieces2.setDisable(true);
    }

    @FXML
    private void playerInputEvent(MouseEvent mouseEvent) throws IOException {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        boolean isTurnToSwitch = false;
        System.out.println(this.game.getGameState());
        System.out.println(this.playerOne.getAmountOfPieces());
        System.out.println(this.playerTwo.getAmountOfPieces());

        switch (game.getGameState()) {
            case SET  -> isTurnToSwitch = setPieceAtSelectedLocation(x, y);
            case MOVE, JUMP -> handleStateMoveAndJump(x, y);
            case TAKE -> {
                if (removeHighlightedPiece(x, y)) {
                    changeColorFromHighlightedPieces(Color.RED, this.game.getCurrentPlayerColor() == 1 ? Color.GRAY : Color.WHITE);
                    isTurnToSwitch = true;
                }
            }
            case OVER -> {
                System.out.println(this.game.getCurrentPlayerColor());
            }
        }

        if (isTurnToSwitch) {
            this.game.switchTurn();
        }

        if (game.getGameState().equals(GameState.OVER)) {
            int winnerColor = this.game.getCurrentPlayerColor() == 1 ? 2 : 1;
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Congratulations " + (winnerColor == 1 ? lblPlayer1.getText() : lblPlayer2.getText()) + ", you WON!!!");

            GameRepository gameRepository = new GameRepository();
            Game game;
            if (winnerColor == 1 && playerOneIsWhite) {
                game = new Game(CurrentGame.getInstance().getPlayer1Id(), CurrentGame.getInstance().getPlayer2Id());
                System.out.println("Links gwonna");
            }
            else {
                game = new Game(CurrentGame.getInstance().getPlayer2Id(), CurrentGame.getInstance().getPlayer1Id());
                System.out.println("Rechts gwonna");
            }
            gameRepository.insert(game);

            alert.showAndWait();
            Stage stage = (Stage) lblPhase.getScene().getWindow();
            stage.setScene(new Scene(loadFXML("home"), 800, 800));
        }

        lblPhase.setText("You can " + game.getGameState().toString());
    }

    private void handleStateMoveAndJump(double x, double y) {
        if (this.currentlySelected != null && moveSelectedPieceToNextPositionOrDropIt(x, y)) {
            game.switchTurn();
            return;
        }

        this.currentlySelected = gameBoard.getPieceFromSelectedCoordinates(x, y, game.getCurrentPlayerColor() == 1 ? Color.WHITE : Color.GRAY);

        if (this.currentlySelected != null && this.game.getGameState() != GameState.TAKE) {
            this.currentlySelected.setFill(Color.RED);
        }
    }

    private boolean moveSelectedPieceToNextPositionOrDropIt(double x, double y) {
        this.currentlySelected.setFill(this.game.getCurrentPlayerColor() == 1 ? Color.WHITE : Color.GRAY);

        if (!this.gameBoard.containsCoordinate(x, y)) {
            this.currentlySelected = null;
            return false;
        }

        Position from = this.gameBoard.convertCoordinateToPosition(this.currentlySelected.getCenterX(), this.currentlySelected.getCenterY());
        Position to   = this.gameBoard.convertCoordinateToPosition(x, y);

        if (!game.movePiece(game.getCurrentPlayerColor(), from, to)) {
            this.currentlySelected = null;
            return false;
        }

        this.movesForReplay.add(new Move(this.currentlySelected.getCenterX(), this.currentlySelected.getCenterY(), x, y));
        this.gameBoard.getChildren().remove(this.currentlySelected);
        drawCircleAtPos(to);

        if (Logic.activatesMill(this.game, from, to)) {
            this.game.setGameState(GameState.TAKE);
            highlightTakeablePieces();
            return false;
        }

        return true;
    }

    private void highlightTakeablePieces() {
        this.takeablePieces = Logic.getTakeablePieces(game, game.getCurrentPlayerColor() == 1 ? 2 : 1);

        if (this.takeablePieces.size() == 0) {
            this.game.updateGameState();
            return;
        }

        changeColorFromHighlightedPieces(this.game.getCurrentPlayerColor() == 1 ? Color.GRAY : Color.WHITE, Color.RED);
    }

    private boolean removeHighlightedPiece(double x, double y) {
        if (!gameBoard.containsCoordinate(x, y)) {
            return false;
        }

        Position pos = gameBoard.convertCoordinateToPosition(x, y);

        if (this.takeablePieces.contains(pos)) {
            gameBoard.getChildren().remove(gameBoard.getPieceFromSelectedCoordinates(x, y, Color.RED));
            game.removePiece(pos);
            return true;
        }

        return false;
    }

    private boolean setPieceAtSelectedLocation(double x, double y) {
        boolean isTurnToSwitch = false;

        if (!gameBoard.containsCoordinate(x, y)) {
            return false;
        }
        Position pos = gameBoard.convertCoordinateToPosition(x, y);

        if (game.setPiece(game.getCurrentPlayerColor(), pos)) {
            this.movesForReplay.add(new Move(x, y, 0, 0));
            drawCircleAtPos(pos);

            if (Logic.activatesMill(game, null, pos)) {
                highlightTakeablePieces();
                game.setGameState(GameState.TAKE);
            } else {
                isTurnToSwitch = true;
                game.updateGameState();
            }
        }

        return isTurnToSwitch;
    }

    private void changeColorFromHighlightedPieces(Color fColor, Color tColor) {
        this.currentlySelected = null;
        double boardSize = Math.min(gameBoard.getWidth(), gameBoard.getHeight()) - 2 * 50;
        double aSixth = boardSize / 6;

        this.takeablePieces.stream()
                .map(p -> gameBoard.getPieceFromSelectedCoordinates(
                        50 + p.getX() * ((3 - p.getZ()) * aSixth) + p.getZ() * aSixth,
                        50 + p.getY() * ((3 - p.getZ()) * aSixth) + p.getZ() * aSixth,
                        fColor))
                .forEach(c -> {
                    if (c != null)
                        c.setFill(tColor);
                });
    }

    private void drawCircleAtPos(Position pos) {
        double boardSize = Math.min(gameBoard.getWidth(), gameBoard.getHeight()) - 2 * 50;
        double aSixth = boardSize / 6;

        double x = 50 + pos.getX() * ((3 - pos.getZ()) * aSixth) + pos.getZ() * aSixth;
        double y = 50 + pos.getY() * ((3 - pos.getZ()) * aSixth) + pos.getZ() * aSixth;

        if (game.getCurrentPlayerColor() == 1) {
            gameBoard.drawIntersection(x, y, Color.WHITE, 9);
        }
        else {
            gameBoard.drawIntersection(x, y, Color.GRAY, 9);
        }
    }
}