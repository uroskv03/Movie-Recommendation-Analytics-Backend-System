package rs.ac.bg.etf.sab.student;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.sab.operations.UsersOperations;
import java.sql.*;

public class nu220058_UsersOperations implements UsersOperations {

	@Override
	public Integer addUser(String username) {
		Connection conn = DB.getInstance().getConnection();
        String query = "INSERT INTO [User] (Username) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { 
        	return null; 
        }
        return null;
	}

	@Override
	public boolean doesUserExist(String username) {
		return getUserId(username) != null;
	}

	@Override
	public List<Integer> getAllUserIds() {
		List<Integer> ids = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String  query = "SELECT IdU FROM [User]";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) { 
        	e.printStackTrace(); 
        }
        return ids;
	}

	@Override
	public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer userId) {
		List<Integer> recommended = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();

        String favGenresQuery = "SELECT mg.IdG FROM Rating r JOIN MovieGenre mg ON r.IdM = mg.IdM " +
                                "WHERE r.IdU = ? GROUP BY mg.IdG HAVING AVG(CAST(r.Score AS DECIMAL(10,3))) >= 8";
        
        String query = 
            "SELECT DISTINCT m.IdM, m.AverageRating FROM Movie m " +
            "JOIN MovieGenre mg ON m.IdM = mg.IdM " +
            "WHERE mg.IdG IN (" + favGenresQuery + ") " + 
            "AND m.IdM NOT IN (SELECT IdM FROM Rating WHERE IdU = ?) " + 
            "AND m.IdM NOT IN (SELECT IdM FROM Watchlist WHERE IdU = ?) " + 
            "AND ( " +
            "    ((SELECT COUNT(*) FROM Rating WHERE IdM = m.IdM) >= 4 AND m.AverageRating >= 7.5) " + 
            "    OR " +
            "    ((SELECT COUNT(*) FROM Rating WHERE IdM = m.IdM) < 4 AND m.AverageRating >= 9.0) " + 
            ") " +
            "ORDER BY m.AverageRating DESC, m.IdM ASC";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId); 
            ps.setInt(2, userId); 
            ps.setInt(3, userId); 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) recommended.add(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return recommended;
	}

	@Override
    public Integer getRewards(Integer userId) {
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT ISNULL(Rewards, 0) Rewards FROM [User] WHERE IdU = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

	@Override
	public List<String> getThematicSpecializations(Integer userId) {
		List<String> specializations = new ArrayList<>();
	    Connection conn = DB.getInstance().getConnection();
	    
	    String query = "SELECT t.Name FROM Tag t " +
	                   "JOIN MovieTag mt ON t.IdT = mt.IdT " +
	                   "JOIN Rating r ON mt.IdM = r.IdM " +
	                   "WHERE r.IdU = ? AND r.Score >= 8 " +
	                   "GROUP BY t.Name HAVING COUNT(mt.IdM) >= 2";
	    try (PreparedStatement ps = conn.prepareStatement(query)) {
	        ps.setInt(1, userId);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) specializations.add(rs.getString(1));
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return specializations;
	}

	@Override
    public String getUserDescription(Integer userId) {
        Connection conn = DB.getInstance().getConnection();
        int movieCount = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Rating WHERE IdU = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) movieCount = rs.getInt(1);
            }
        } catch (SQLException e) { return "undefined"; }

        if (movieCount < 10) return "undefined";

        int tagCount = 0;
        String tagQuery = "SELECT COUNT(DISTINCT mt.IdT) FROM Rating r " +
                          "JOIN MovieTag mt ON r.IdM = mt.IdM WHERE r.IdU = ?";
        try (PreparedStatement ps = conn.prepareStatement(tagQuery)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tagCount = rs.getInt(1);
            }
        } catch (SQLException e) { return "undefined"; }

        if (tagCount >= 10) return "curious";
        else return "focused";
    }

	@Override
	public Integer getUserId(String username) {
		Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdU FROM [User] WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { 
        	e.printStackTrace(); 
        }
        return null;
	}

	@Override
	public Integer removeUser(Integer id) {
		Connection conn = DB.getInstance().getConnection();
		try {
			conn.setAutoCommit(false);
			String[] tables = {"Rating", "Watchlist"};
			
			for (String table : tables) {
	            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE IdU  = ?")) {
	                ps.setInt(1, id);
	                ps.executeUpdate();
	            }
	        }
			
	        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM [User] WHERE IdU  = ?")) {
	            ps.setInt(1, id);
	            int rows = ps.executeUpdate();
	            conn.commit();
	            return rows > 0 ? id : null;
	        }
        } catch (SQLException e) { 
        	try { 
        		conn.rollback(); 
        	} 
        	catch (SQLException ex) {
        	}
        	return null;
        } finally {
            try { 
            	conn.setAutoCommit(true); 
            } 
            catch (SQLException ex) {
            }
        }
	}

	@Override
	public Integer updateUser(Integer id, String newUsername) {
		 Connection conn = DB.getInstance().getConnection();
	        String query = "UPDATE [User] SET Username = ? WHERE IdU = ?";
	        try (PreparedStatement ps = conn.prepareStatement(query)) {
	            ps.setString(1, newUsername);
	            ps.setInt(2, id);
	            return ps.executeUpdate() > 0 ? id : null;
	        } catch (SQLException e) { 
	        	return null; 
	        }
	}

}
