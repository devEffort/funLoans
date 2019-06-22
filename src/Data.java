
import java.util.LinkedList;


/**
Data object to hold a Loan
 */
class Loan {
	int id;
	int amount;
	float interestRate;
	float defaultLikelihood;
	String state;
}

/**
 * Simple class to return the status of the loan
 * This will help clarify why the loan was denied (if denied)
 */
class LoanStatus {
	boolean isApproved;
	String reasonForDenial;

	public LoanStatus(boolean isApproved, String reasonForDenial){
		this.isApproved = isApproved;
		this.reasonForDenial = reasonForDenial;
	}
}

/**
Data object to hold facility and its list of covenants
 */
class Facility implements Comparable<Facility> {
	int bankId;
	int facilityId;
	float interestRate;
	int amountAvailableToLoan;
	LinkedList<String> bannedStates = new LinkedList<String>();
	float maxDefaultLikelihood;
	
	/**
	 * Function to confirm the loan's eligibility at this facility
	 * Checks for interest rate, amount available to loan, and two covenants
	 * In future, once we add the Strategy design patterns to covenants this can run through them
	 * @param loan
	 * @return
	 */
	public LoanStatus getLoanEligibility(Loan loan) {
		// We shouldn't loan if interest rate match to get a net positive yield
		if (loan.interestRate <= this.interestRate)
			return new LoanStatus(false, "Interest rate too high.");

		if (loan.amount > this.amountAvailableToLoan)
			return new LoanStatus(false, "Funds not available.");

		if (loan.defaultLikelihood > this.maxDefaultLikelihood)
			return new LoanStatus(false, "Default likelihood mismatch.");

		if (this.bannedStates.contains(loan.state))
			return new LoanStatus(false, "Unapproved state");

		return new LoanStatus(true, "");
	}

	@Override
	public int compareTo(Facility facility) {
		if (this.interestRate > facility.interestRate)
			return 1;
		if (this.interestRate <= facility.interestRate)
			return -1;
		return 0;
	}
}
