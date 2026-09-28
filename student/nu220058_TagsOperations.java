package rs.ac.bg.etf.sab.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.sab.operations.TagsOperations;

public class nu220058_TagsOperations implements TagsOperations {

    @Override
    public Integer addTag(Integer movieId, String tag) {
        Connection conn = DB.getInstance().getConnection();
        try {
            if (!movieExists(movieId)) return null;
            Integer tagId = getOrCreateTagId(tag);
            if (tagId == null) return null;
            if (hasTag(movieId, tag)) return null;
            
            String query = "INSERT INTO MovieTag (IdM, IdT) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, movieId);
                ps.setInt(2, tagId);
                ps.executeUpdate();
                return movieId;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeTag(Integer movieId, String tag) {
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE mt FROM MovieTag mt JOIN Tag t ON mt.IdT = t.IdT WHERE mt.IdM = ? AND t.Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            int rows = ps.executeUpdate();
            return rows > 0 ? movieId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public int removeAllTagsForMovie(Integer movieId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM MovieTag WHERE IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public boolean hasTag(Integer movieId, String tag) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT * FROM MovieTag mt JOIN Tag t ON mt.IdT = t.IdT WHERE mt.IdM = ? AND t.Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> getTagsForMovie(Integer movieId) {
        List<String> tags = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT t.Name FROM Tag t JOIN MovieTag mt ON t.IdT = mt.IdT WHERE mt.IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tags.add(rs.getString(1));
            }
        } catch (SQLException e) { 
        	e.printStackTrace(); 
        }
        return tags;
    }

    @Override
    public List<Integer> getMovieIdsByTag(String tag) {
        List<Integer> ids = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT mt.IdM FROM MovieTag mt JOIN Tag t ON mt.IdT = t.IdT WHERE t.Name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        } catch (SQLException e) { 
        	e.printStackTrace(); 
        }
        return ids;
    }

    @Override
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Name FROM Tag";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) tags.add(rs.getString(1));
        } catch (SQLException e) { 
        	e.printStackTrace(); 
        }
        return tags;
    }

    private boolean movieExists(Integer movieId) throws SQLException {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Movie WHERE IdM = ?")) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Integer getOrCreateTagId(String tag) throws SQLException {
        Connection conn = DB.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT IdT FROM Tag WHERE Name = ?")) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Tag (Name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tag);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

}
