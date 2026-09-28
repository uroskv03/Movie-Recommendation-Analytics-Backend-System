package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.GeneralOperations;

import java.sql.*;

public class nu220058_GeneralOperations implements GeneralOperations {

	@Override
	public void eraseAll() {
		Connection conn = DB.getInstance().getConnection();
        String[] tables = {
            "Watchlist", "Rating", "MovieTag", "MovieGenre", 
            "Tag", "Genre", "Movie", "[User]"
        };
        
        try (Statement st = conn.createStatement()) {
            for (String table : tables) {
                st.executeUpdate("DELETE FROM " + table); 
                try {
                    st.executeUpdate("DBCC CHECKIDENT ('" + table + "', RESEED, 0)"); //reset da id ide od 0
                } catch (SQLException e) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

	}

}
