package rs.ac.bg.etf.sab.student;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.sab.operations.GenresOperations;
import java.sql.*;

public class nu220058_GenresOperations implements GenresOperations {

	@Override
	public Integer addGenre(String name) {
		Connection conn = DB.getInstance().getConnection();	
		String query = "INSERT INTO Genre (Name) VALUES (?)";
		try (
			PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS))
		{
			ps.setString(1, name);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
                return rs.getInt(1); 
            }
		} catch (SQLException e) {
			return null;
		}
		return null;
	}

	@Override
	public boolean doesGenreExist(String name) {
		return getGenreId(name) != null;
	}

	@Override
	public List<Integer> getAllGenreIds() {
		List<Integer> ret = new ArrayList<>();
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT idG FROM Genre";
		try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
            	ret.add(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
		return ret;
	}

	@Override
	public Integer getGenreId(String name) {
		Connection conn = DB.getInstance().getConnection();
        String query = "SELECT idG FROM Genre WHERE Name = ?";
        try (
			PreparedStatement  ps = conn.prepareStatement(query))
        {
        	ps.setString(1, name);
        	try(ResultSet rs = ps.executeQuery()){
        		if(rs.next()) return rs.getInt(1);
        	}
		} catch (SQLException e) {
			return null;
		}
		return null;
	}

	@Override
	public Integer removeGenre(Integer id) {
        Connection conn = DB.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM MovieGenre WHERE IdG = ?")) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM Genre WHERE IdG = ?")) {
                ps2.setInt(1, id);
                int rowsDeleted = ps2.executeUpdate();
                conn.commit();
                return rowsDeleted > 0 ? id : null;
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
	public Integer updateGenre(Integer id, String newName) {
		Connection conn = DB.getInstance().getConnection();
		String query = "UPDATE Genre SET Name = ? WHERE IdG = ?";
		try (
			PreparedStatement ps = conn.prepareStatement(query))
		{
			ps.setString(1, newName);
			ps.setInt(2, id);
			int rowsUpdated = ps.executeUpdate();
			return rowsUpdated > 0 ? id : null;
		} catch (SQLException e) {
			return null;
		}
	}

}
