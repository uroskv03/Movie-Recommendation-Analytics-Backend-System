package rs.ac.bg.etf.sab.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.sab.operations.MoviesOperations;

public class nu220058_MoviesOperations implements MoviesOperations {

	@Override
	public Integer addGenreToMovie(Integer movieId, Integer genreId) {
		Connection conn  = DB.getInstance().getConnection();
		
		String check = "SELECT 1 FROM MovieGenre WHERE IdM = ? AND IdG = ?";
		try (PreparedStatement psCheck = conn.prepareStatement(check)) {
            psCheck.setInt(1, movieId);
            psCheck.setInt(2, genreId);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) return null; 
            }
        } catch (SQLException e) { 
        	return null; 
        }
		String query = "INSERT INTO MovieGenre (IdM, IdG) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setInt(2, genreId);
            ps.executeUpdate();
            return movieId;
        } catch (SQLException e) { 
        	return null; 
        }
	}

	@Override
	public Integer addMovie(String title, Integer genreId, String director) {
		Connection conn = DB.getInstance().getConnection();
		try {
            conn.setAutoCommit(false); 
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT 1 FROM Genre WHERE IdG = ?")) {
                psCheck.setInt(1, genreId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null; 
                    }
                }
            }
            Integer movieId = null;
            String movieQuery = "INSERT INTO Movie (Title, Director) VALUES (?, ?)";
            try (PreparedStatement psMovie = conn.prepareStatement(movieQuery, Statement.RETURN_GENERATED_KEYS)) {
                psMovie.setString(1, title);
                psMovie.setString(2, director);
                psMovie.executeUpdate();
                try (ResultSet rs = psMovie.getGeneratedKeys()) {
                    if (rs.next()) movieId = rs.getInt(1);
                }
            }

            if (movieId == null) {
                conn.rollback();
                return null;
            }

            String genreQuery = "INSERT INTO MovieGenre (IdM, IdG) VALUES (?, ?)";
            try (PreparedStatement psGenre = conn.prepareStatement(genreQuery)) {
                psGenre.setInt(1, movieId);
                psGenre.setInt(2, genreId);
                psGenre.executeUpdate();
            }

            conn.commit();
            return movieId;

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
	public List<Integer> getAllMovieIds() {
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT IdM FROM Movie";
		List<Integer> movies = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(query)){
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					movies.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return movies;
	}

	@Override
	public List<Integer> getGenreIdsForMovie(Integer movieId) {
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT IdG FROM MovieGenre WHERE IdM = ?";
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, movieId);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

	@Override
	public List<Integer> getMovieIds(String title, String director) {
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT IdM FROM Movie WHERE Title = ? AND Director = ?";
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, title);
			ps.setString(2, director);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

	@Override
	public List<Integer> getMovieIdsByDirector(String director) {
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT IdM FROM Movie WHERE Director = ?";
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, director);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

	@Override
	public List<Integer> getMovieIdsByGenre(Integer genreId) {
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT IdM FROM MovieGenre WHERE IdG = ?";
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, genreId);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

	@Override
	public String getMovieTrend(Integer movieId) {
		Connection conn = DB.getInstance().getConnection();
		String query = "SELECT Status FROM Movie WHERE IdM = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, movieId);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					return rs.getString(1);
				}
			}
		} catch (SQLException e) {
			return null;
		}
		return null;
	}

	@Override
	public Integer removeGenreFromMovie(Integer movieId, Integer genreId) {
		Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM MovieGenre WHERE IdM = ? AND IdG = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setInt(2, genreId);
            return ps.executeUpdate() > 0 ? movieId : null;
        } catch (SQLException e) { 
        	return null; 
        }
	}

	@Override
	public Integer removeMovie(Integer movieId) {
		Connection conn = DB.getInstance().getConnection();
		try {
			conn.setAutoCommit(false);
			String[] tables = {"MovieGenre", "MovieTag", "Rating", "Watchlist"};
			
			for (String table : tables) {
	            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE IdM = ?")) {
	                ps.setInt(1, movieId);
	                ps.executeUpdate();
	            }
	        }
			
	        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Movie WHERE IdM = ?")) {
	            ps.setInt(1, movieId);
	            int rows = ps.executeUpdate();
	            conn.commit();
	            return rows > 0 ? movieId : null;
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
	public Integer updateMovieDirector(Integer movieId, String director) {
		Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Movie SET Director = ? WHERE IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, director);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0 ? movieId : null;
        } catch (SQLException e) { 
        	return null; 
        }
	}

	@Override
	public Integer updateMovieTitle(Integer movieId, String title) {
		Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Movie SET Title = ? WHERE IdM = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0 ? movieId : null;
        } catch (SQLException e) { 
        	return null; 
        }
	}

}
