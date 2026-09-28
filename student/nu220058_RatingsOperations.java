package rs.ac.bg.etf.sab.student;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.sab.operations.RatingsOperations;
import java.sql.*;

public class nu220058_RatingsOperations implements RatingsOperations {

	@Override
    public boolean addRating(Integer userId, Integer movieId, Integer score) {
        Connection conn = DB.getInstance().getConnection();
        String query = "INSERT INTO Rating (IdU, IdM, Score, DateRated) VALUES (?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setInt(3, score);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

	@Override
    public List<Integer> getRatedMoviesByUser(Integer userId) {
        List<Integer> movieIds = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdM FROM Rating WHERE IdU = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieIds;
    }

	@Override
    public Integer getRating(Integer userId, Integer movieId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Score FROM Rating WHERE IdU = ? AND IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

	@Override
    public List<Integer> getUsersWhoRatedMovie(Integer movieId) {
        List<Integer> userIds = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdU FROM Rating WHERE IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }

	@Override
    public boolean removeRating(Integer userId, Integer movieId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM Rating WHERE IdU = ? AND IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

	@Override
    public boolean updateRating(Integer userId, Integer movieId, Integer newScore) {
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Rating SET Score = ?, DateRated = GETDATE() WHERE IdU = ? AND IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newScore);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);
            int rows = ps.executeUpdate();
            return rows > 0; 
        } catch (SQLException e) {
            return false;
        }
    }

}
