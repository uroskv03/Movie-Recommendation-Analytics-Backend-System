package rs.ac.bg.etf.sab.student;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.PublicModuleTest;
import rs.ac.bg.etf.sab.tests.TestHandler;

public class ManualRunner {
    public static void main(String[] args) {
    	
        GeneralOperations go = new nu220058_GeneralOperations();
        GenresOperations gno = new nu220058_GenresOperations();
        MoviesOperations mo = new nu220058_MoviesOperations();
        RatingsOperations ro = new nu220058_RatingsOperations();
        TagsOperations to = new nu220058_TagsOperations();
        UsersOperations uo = new nu220058_UsersOperations();
        WatchlistsOperations wo = new nu220058_WatchlistsOperations();

        TestHandler.createInstance(gno, mo, ro, to, uo, wo, go);

        Result result = JUnitCore.runClasses(PublicModuleTest.class);   
        //ili promenis u drugu klasu npr GeneralOperations.class

        if (result.wasSuccessful()) {
            System.out.println("Tacno resenje");
        } else {
            System.out.println("Palo je " + result.getFailureCount() + " provera.");
            for (Failure failure : result.getFailures()) {
                System.out.println("--------------------------------------------");
                System.out.println("GREŠKA U: " + failure.getTestHeader());
                System.out.println("PORUKA: " + failure.getMessage());
                System.out.println("DETALJI: " + failure.getTrace());
            }
        }
    }
}