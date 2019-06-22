

// A list of all covenants using the strategy pattern
// Good for adding future covenants and not changing core execution
// LinkedList<FacilityCovenant> covenants;

import java.util.LinkedList;

/*
Interface to capture covenants for a facility - as a Strategy
This is based on the Strategy design pattern
 */
interface FacilityCovenant {
	boolean executeCovenant(Loan loan);
}

/*
Covenant for the default likelihood calculation
 */
class CovenantDefault implements FacilityCovenant {
	float maxDefaultLikelihood;
	public CovenantDefault(float maxDefaultLikelihood) {
		this.maxDefaultLikelihood = maxDefaultLikelihood;
	}

	public boolean executeCovenant(Loan loan) {
		return loan.defaultLikelihood < maxDefaultLikelihood;
	}
}

/*
Covenant for banned states
 */
class CovenantStates implements FacilityCovenant{
	LinkedList<String> states = new LinkedList<String>();
	public CovenantStates(String state){
		this.states.add(state);
	}

	public boolean executeCovenant(Loan loan) {
		return states.contains(loan.state);
	}
}