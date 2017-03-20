import java.util.ArrayList;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

   double p_graph;
   double p_malicious;
   double p_txDistribution;
   int numRounds;
   int round = 0;
   int num_followers; 
   boolean[] followers; 
   Set<Transaction> pendingTransactions;
   
   
   public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
      this.p_graph = p_graph;
      this.p_malicious = p_malicious;
      this.p_txDistribution = p_txDistribution;
      this.numRounds = numRounds;
   }

   public void setFollowees(boolean[] followees) {
	   this.num_followers = followees.length; 
	   this.followers = followees;
   }
   
   public void setPendingTransaction(Set<Transaction> pendingTransactions) {
      this.pendingTransactions = pendingTransactions;
   }

   public Set<Transaction> getProposals() {
      return pendingTransactions;
   }

   public void receiveCandidates(ArrayList<Integer[]> candidates) {     
      for(Integer[] candidate : candidates) {
         Transaction tx = new Transaction(candidate[0]);
         if(!this.pendingTransactions.contains(tx))
            this.pendingTransactions.add(tx);
      }
   }
}
