import java.io.IOException;
import java.util.*;

/**
 Class to handle/mock the stream operations
 This can be enhanced in the future to read from a real data stream pipeline
 This class processes each loan and maps to right facility, and then performs bookkeeping
 */
public class StreamProcessor {
	// Facilities sorted by interest rate
	private ArrayList<Facility> sortedFacilities;
	private TreeMap<Integer, Float> facilityYield = new TreeMap<>();
	TreeMap<Integer, Integer> loanFacility = new TreeMap<>();
	TreeMap<Integer, Integer> facilityYieldFinal = new TreeMap<>();

	/**
	 * Constructor to setup the stream processing context
	 * The context is completely in memory - but needs to be more durable in future
	 */
	public StreamProcessor(String facilityFilePath, String covenantFilePath) {
		// Load the processing context in form of facilities and covenants
		try {
			Hashtable<Integer, Facility> facilities = FileLoader.readFacilities(facilityFilePath, covenantFilePath);
			// Once we have all the facilities and its covenants, build a list of faciities
			// sorted by the interest rate
			// We are optimizing for reading/access of faciities to match incoming loans
			// So sort it once - nlg(n)
			sortedFacilities = new ArrayList<Facility>(facilities.values());
			sortedFacilities.sort((o1, o2) -> Math.round(o1.compareTo(o2)));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to handle the stream of loans
	 * Iterate through every object and invoke processing operation
	 * In future this can be subscribing to a data pipeline
	 */
	void loanStreamSubscriber(LinkedList<Loan> loans) {
		// Check the loan against every facility to match its covenants
		// For now we will be looping over
		// This can be an actual subscriber to a streaming pipeline
		for (Loan l: loans) {
			this.computeOnStreamedInstance(l);
		}

	}

	/**
	 * Operation to handle an instance of the streamed data
	 */
	private void computeOnStreamedInstance(Loan l) {
		for (Facility f: sortedFacilities) {
			// Check if loan is eligible - and then book-keeping if yes
			if (f.getLoanEligibility(l).isApproved) {
				this.manageLoanAssignmentBookKeeping(l, f);
				return;
			}
		}
		// Unassigned loan scenario
		this.manageLoanAssignmentBookKeeping(l, null);
	}

	/**
	 * Manage the book keeping of assigning and updating loan assignments
	 * @param loan
	 * @param facility
	 */
	private void manageLoanAssignmentBookKeeping(Loan loan, Facility facility) {
		
		// Manage unassigned loan
		if (facility == null) {
			this.loanFacility.put(loan.id, 0);
		}
		else {
			this.loanFacility.put(loan.id, facility.facilityId);

			// Reduce the loan amount available to trade
			facility.amountAvailableToLoan -= loan.amount;

			// This is where a Strategy of convenants will be useful to just iterate through

			// Manage loan yield calculation and assign to facility
			float yield = this.facilityYield.getOrDefault(facility.facilityId, Float.valueOf("0"));
			yield += this.getFacilityYieldForLoan(loan, facility);

			this.facilityYield.put(facility.facilityId, yield);
		}
	}

	/**
	 * Calculate the yield for loan and facility
	 * @param loan
	 * @param facility
	 * @return
	 */
	private float getFacilityYieldForLoan(Loan loan, Facility facility) {
		return (1 - loan.defaultLikelihood) * loan.interestRate * loan.amount
				- (loan.defaultLikelihood * loan.amount)
				- (facility.interestRate * loan.amount);
	}

	/**
	 * Convert hash table into Map<String, String> useful for CSV writing
	 */
	void roundOffFinalCalculations() {
		this.facilityYield.entrySet()
				.iterator()
				.forEachRemaining(E -> this.facilityYieldFinal.put(
						E.getKey(), Math.round(E.getValue())
				));
	}
}
