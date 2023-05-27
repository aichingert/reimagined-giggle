package at.htlleonding.mill.db;

import at.htlleonding.mill.model.Move;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoveRepository implements Persistent<Move> {
    private DataSource dataSource = Database.getDataSource();

    @Override
    public void save(Move user) {
        if (user.getId() == null) {
            this.insert(user);
        } else {
            this.update(user);
        }
    }

    @Override
    public void update(Move move) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE M_MOVE SET M_FX=?, M_FY=?, M_TX=?, M_TY=? WHERE M_ID=?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setDouble(1, move.getFx());
            statement.setDouble(2, move.getFy());
            statement.setDouble(3, move.getTx());
            statement.setDouble(4, move.getTy());
            statement.setLong(5, move.getId());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Update of M_MOVE failed, no rows affected");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(Move move) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO M_MOVE (M_FX, M_FY,M_TX, M_TY) VALUES (?,?,?,?)";

            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setDouble(1, move.getFx());
            statement.setDouble(2, move.getFy());
            statement.setDouble(3, move.getTx());
            statement.setDouble(4, move.getTy());

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Update of M_MOVE failed, no rows affected");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    move.setId(keys.getLong(1));
                } else {
                    throw new SQLException("Insert into M_MOVE failed, no ID obtained");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(long id) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "DELETE FROM M_MOVE WHERE M_ID=?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);

            if (statement.executeUpdate() == 0) {
                throw new SQLException("Deletion of M_MOVE failed, no rows affected");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Move> findAll() {
        List<Move> moves = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM M_MOVE";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet result = preparedStatement.executeQuery();

            while(result.next()) {
                Move move = new Move(
                        result.getDouble("M_FX"),
                        result.getDouble("M_FY"),
                        result.getDouble("M_TX"),
                        result.getDouble("M_TY"));

                move.setId(result.getLong("M_ID"));

                moves.add(move);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return moves;
    }

    @Override
    public Move findById(long id) {
        Move move = null;

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM M_MOVE WHERE M_ID=?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                move = new Move(
                        result.getDouble("M_FX"),
                        result.getDouble("M_FY"),
                        result.getDouble("M_TX"),
                        result.getDouble("M_TY"));
                move.setId(result.getLong("M_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return move;
    }
}