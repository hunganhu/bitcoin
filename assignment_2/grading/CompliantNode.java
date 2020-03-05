import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	double p_graph;
	double p_malicious;
	double p_txDistribution;
	int numRounds;
	
//	int round = 0; 
//	int num_followees; 
	boolean[] followees; 
	Set<Transaction> pendingTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    	this.p_graph = p_graph;
    	this.p_malicious = p_malicious;
    	this.p_txDistribution = p_txDistribution;
    	this.numRounds = numRounds;
    }

    // NOTE: Node is an interface and does not have a constructor.
    // However, your CompliantNode.java class requires a 4 argument
    // constructor as defined in Simulation.java

    // followees[i] is True iff this node follows node i
    public void setFollowees(boolean[] followees) {
      	this.followees = followees;
    	return;
    }

    //initialize proposal list of transactions
    public void setPendingTransaction (Set<Transaction> pendingTransactions) {
    	this.pendingTransactions = pendingTransactions;
    	return;
    }

    // return proposals to send to my followers.
    // REMEMBER: After final round, behavior of getProposals changes and
    // it should return the transactions upon which consensus has been reached.
    public Set<Transaction> getProposals () {
        return this.pendingTransactions;
    }

    // receive candidates from other nodes.
    public void receiveCandidates (ArrayList<Integer[]> candidates) {
    	// accept all transactions from all followees
        for(Integer[] candidate : candidates) {
        	Transaction tx = new Transaction(candidate[0].intValue());
        	if(!this.pendingTransactions.contains(tx))
        		this.pendingTransactions.add(tx);
         }

    	return;
    }

}
