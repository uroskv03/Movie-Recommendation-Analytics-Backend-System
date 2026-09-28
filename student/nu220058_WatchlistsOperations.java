package rs.ac.bg.etf.sab.student;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import rs.ac.bg.etf.sab.operations.WatchlistsOperations;

public class nu220058_WatchlistsOperations implements WatchlistsOperations {

	@Override
    public boolean addMovieToWatchlist(Integer userId, Integer movieId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "INSERT INTO Watchlist (IdU, IdM) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

	 @Override
    public List<Integer> getMoviesInWatchlist(Integer userId) {
        List<Integer> movies = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdM FROM Watchlist WHERE IdU = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movies.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

	 @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer movieId) {
        List<Integer> users = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdU FROM Watchlist WHERE IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

	@Override
    public boolean isMovieInWatchlist(Integer userId, Integer movieId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT 1 FROM Watchlist WHERE IdU = ? AND IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        } catch (SQLException e) {
            return false;
        }
    }

	@Override
    public boolean removeMovieFromWatchlist(Integer userId, Integer movieId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM Watchlist WHERE IdU = ? AND IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

}
