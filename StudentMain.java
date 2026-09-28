package rs.ac.bg.etf.sab;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;
import rs.ac.bg.etf.sab.student.*;

public class StudentMain {
    public static void main(String[] args) throws Exception {
    	GeneralOperations generalOperations = new nu220058_GeneralOperations();
    	GenresOperations genresOperations = new nu220058_GenresOperations();
        MoviesOperations moviesOperations = new nu220058_MoviesOperations();
        RatingsOperations ratingsOperation = new nu220058_RatingsOperations();
        TagsOperations tagsOperations = new nu220058_TagsOperations();
        UsersOperations usersOperations = new nu220058_UsersOperations();
        WatchlistsOperations watchlistsOperations = new nu220058_WatchlistsOperations();
    	

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);
        TestRunner.runTests();
    }
}