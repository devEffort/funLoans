
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

/**
 * Class to handle file reads and convert CSV data into useful data structure representation
 * This class will create data structures that are optimized for processing (query time)
 */
public class FileLoader {

	/**
  	 * Function to read loans csv from disk
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static LinkedList<Loan> readLoans(String filePath) throws IOException {
		LinkedList<Loan> loans = new LinkedList<>();
		try {
			Reader in = new FileReader(filePath);
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
				Loan loan = new Loan();
				loan.interestRate = Float.parseFloat(record.get(0));
				loan.amount = Integer.parseInt(record.get(1));
				loan.id = Integer.parseInt(record.get(2));
				loan.defaultLikelihood = Float.parseFloat(record.get(3));
				loan.state = record.get(4);

				loans.add(loan);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return loans;
	}

	/**
	 * Function to read facilities and their covenants csv from disk
	 * We also combine facilities and covenants to join them together for easy access
	 * @param facilityFilePath
	 * @param covenantFilePath
	 * @return
	 * @throws IOException
	 */
	public static Hashtable<Integer, Facility> readFacilities(String facilityFilePath, String covenantFilePath) throws IOException {
		Hashtable<Integer, Facility> facilities = new Hashtable<>();

		try {
			// Read facilities
			Reader in = new FileReader(facilityFilePath);
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : records) {
				Facility facility = new Facility();
				facility.amountAvailableToLoan = Math.round(Float.parseFloat(record.get(0)));
				facility.interestRate = Float.parseFloat(record.get(1));
				facility.facilityId = Integer.parseInt(record.get(2));
				facility.bankId = Integer.parseInt(record.get(3));

				facilities.put(facility.facilityId, facility);
			}

			// Read covenants and assign them to facilities
			in = new FileReader(covenantFilePath);
			Iterable<CSVRecord> cRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : cRecords) {
				int fId = Integer.parseInt(record.get(0));
				// Fetch the facility based on id and augment the covenants
				Facility f = facilities.get(fId);
				// Some records might not have a covenant column, so we need to check
				if (!record.get(1).isEmpty())
					f.maxDefaultLikelihood = Float.parseFloat(record.get(1));

				if (!record.get(3).isEmpty())
					f.bannedStates.add(record.get(3));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return facilities;
	}
	
	/**
	 * Write the headers and data (of CSV format) to file on disk
	 * @param data
	 * @param filePath
	 * @param headers
	 */
	public static void writeCSVToDisk(TreeMap data, String filePath, String... headers) {
		try {
			FileWriter out = new FileWriter(filePath);
			try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.RFC4180
					.withHeader(headers)))
			{
				data.forEach((val1, val2) -> {
					try {
						printer.printRecord(val1, val2);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

