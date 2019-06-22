
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) throws IOException {
	    // Read the loans
        LinkedList<Loan> loans = FileLoader.readLoans("large/loans.csv");

        // Start the stream processor
        StreamProcessor sProcessor = new StreamProcessor("large/facilities.csv",
                "large/covenants.csv");
        // Start listening to the 'stream'
        sProcessor.loanStreamSubscriber(loans);
        sProcessor.roundOffFinalCalculations();

        // Write data to CSV files
        FileLoader.writeCSVToDisk(sProcessor.facilityYieldFinal,
                "large/yields.csv", "facility_id", "expected_yield");
        FileLoader.writeCSVToDisk(sProcessor.loanFacility,
                        "large/assignments.csv", "loan_id", "facility_id");
    }
}
