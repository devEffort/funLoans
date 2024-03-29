**Solution**

Simple Java project with an external library for CSV parsing.

To run, simply compile and run the Main.java. 





***1*. How long did you spend working on the problem? What did you find to be the most difficult part?**


The first working solution took close to 4 hours. It was a combination of getting the code right as well as making sure the CSV representations were figured out. 

I spent time at the beginning, to figure out the best data representation (in form of in memory data structure), so as to simplify the actual calcuations and assignment logic. For me building a simple solution (along with data representation) on a white board before coding helped. 

<br/>
<br/>
<br/>



***2*. How would you modify your data model or code to account for an eventual introduction of new, as-of-yet unknown types of covenants, beyond just maximum default likelihood and state restrictions?**

To enable dynamic covenants to be introduced for a Facility, we need to think about two things:

1. Enable a config or database table or file based covenant representation: 

This will allow picking and applying covenant conditions to the logic without the need for major refactoring. In a simple change, each covenant can be represented as a row in a table/file with a primary key/index of facilityId. This way the business logic can ask for all covenants for a facility and execute the conditions. As long as covenants are added to the external representation, they will be executed at runtime.  

In an ideal world, there is an IDL (interface definition language) that facilities are able to build so that they can define the rules of business in a user friendly language. This can then be translated into simple arithmatic or comparison rules in code. 

2. In code, covenants can be introduced in the form of strategies (as part of Strategy design pattern:

https://en.wikipedia.org/wiki/Strategy_pattern). 
Strategies can be introduced as external libraries/dependencies (a typical design-by-interface principle) so that the core business logic can run through all of the covenants and apply them at runtime. 

In a much future distributed systems world, covenant execution can be hosted as an independent component/service. This will allow de-coupling of covenant evaluation.  

<br/>
<br/>
<br/>

***3*. How would you architect your solution as a production service wherein new facilities can be introduced at arbitrary points in time. Assume these facilities become available by the finance team emailing your team and describing the addition with a new set of CSVs.**

To make this a production quality service, the concerns around loading of facilities, covenants, loans have to seperated from business logic. Currently, the facilities are loaded in memory as part of 'stream processing context'. There is a wide range of things we can do to make this more dynamic. Some examples: 
Enable facility CSV as an external dependency that is being 'watched' by the core service, such that when new CSV file (or version) is introduced the service hot reloads, re-reads the latest file in memory. This way whenever new CSV changes are introduced, the service will have it before processing the next loan. 

Another larger change can be to translate the facility information into a data store instead of on disk CSV. Depending on the actual store and query capabiilty, the service can request batches/pages of facilities in order of interest rate. Introducing a separate data store can also help delegate the facility filtering responsibility, away from core business logic. 

   <br/>
   <br/>
   <br/>


***4*. Your solution most likely simulates the streaming process by directly calling a method in your code to process the loans inside of a for loop. What would a REST API look like for this same service? Stakeholders using the API will need, at a minimum, to be able to request a loan be assigned to a facility, and read the funding status of a loan, as well as query the capacities remaining in facilities.**

The REST API will be on the domain LOAN with the following schema/signature 

**POST  /api.affirm.com/[version]/loans/process**

Data: 
	
	loanId (int)
	loanAmount (int|double)
	interestRate (float)
	state (two character string representation)

Response: 

	loanApprovalStatus : APPROVED | DENIED 
	approvalDetails: (structure with children fields)
		facilityId (id)
		bankId	(id)
	denialDetails: 
		"NOT ENOUGH FUNDS" | "BANNED STATE" | "HIGH INTEREST RATE" | "DEFAULT LIKELIHOOD HIGH" | ""

_There is another technique to have a composition based response which either has approvalDetails or denialDetails (one of them instead of both). Such a technique is not always appreciated by SDKs and clients._  



**GET /api.affirm.com/[version]/facilities/[id]**

Response: 

	facilityName (string)
	bankId (int)
	bankName (string)
	remainderBalance (int|double)
	currentYield (int|double)
	.. 

	
<br/>
<br/>
<br/>


***5*. How might you improve your assignment algorithm if you were permitted to assign loans in batch rather than streaming? We are not looking for code here, but pseudo code or description of a revised algorithm appreciated.**

Having a batch of loans (instead of a single loan) to assign, allows us to look ahead. This scenario can be leveraged to maximize yields by prioritizing loans with highest interest rates to be matched first with facilities. 

e.g. Consider we get a batch of 10 loans to assign to facilities. 

First sort these loans in decreasing order of interest rates, so that the first one has the highest interest rate. 

Now start matching these loans (starting with the one with highest interest rate) to faciities - already ordered with lowest interest rate on top.  

This will increase the possibility of using available funds in a facility for maximum yield. 

 <br/>
 <br/>
 <br/>


***6*. Discuss your solution’s runtime complexity.**

Reading loans - `O(n)`. n number of loans in file.

Read facilities and covenants and de-normalizing - `O(n)`, n is number of facilities. Because we are using a hashtable to store facilities, we are essentially fetching and updating faciities in the hashtable as we read the covenants. 

Sorting facilities - `O(klgk)`. Depending on the choice of inbuilt sort, this can be reduced. e.g bucket or counting sort modified for floats can help.  

Assigning a single loan to facilities - worst case `O(k)`. k is number of facilities. 

If we remove the facilities that do not have any available funds to loan (from the sorted list), then the complexity is further reduced to be `k-t` (where `t` is number of faciities with 0 available balance). 
Considering the number of facilities available, this however might be an premature optimization. 




