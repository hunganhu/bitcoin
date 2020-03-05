import java.util.*;

public class TxHandler {

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	UTXOPool publicLedger;
	public TxHandler(UTXOPool utxoPool) {
		publicLedger = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS
		UTXOPool uniquePool = new UTXOPool();
		ArrayList<Transaction.Input> inputs = tx.getInputs();
		ArrayList<Transaction.Output> outputs = tx.getOutputs();
		double sumInputs = 0.0;
		double sumOutputs = 0.0;
		for (int index = 0; index < inputs.size(); index++) {
			// (1) all inputs claimed by tx are in the current UTXO pool, 
			Transaction.Input in = inputs.get(index);
			UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
			if (!publicLedger.contains(utxo))
				return false;

			Transaction.Output outPrevTX = publicLedger.getTxOutput(utxo);
			RSAKey address = outPrevTX.address;
			sumInputs += outPrevTX.value;
			
			// (2) the signatures on each input of tx are valid, 
			if (!address.verifySignature(tx.getRawDataToSign(index), in.signature))
				return false;
			
			// (3) no UTXO is claimed multiple times by tx, 
			if (uniquePool.contains(utxo))
				return false;
			uniquePool.addUTXO(utxo, outPrevTX);
		}
		

		// (4) all of tx’s output values are non-negative, and
		for (int index = 0; index < outputs.size(); index++) {
			Transaction.Output out = outputs.get(index);
			if (out.value < 0)
				return false;
			sumOutputs += out.value;
		}

		// (5) the sum of tx’s input values is greater than or equal to the sum of   
		//       its output values; and false otherwise.						
		return (sumInputs >= sumOutputs);
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS
		ArrayList <Transaction> validTx = new ArrayList<Transaction>();
		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
				validTx.add(tx);
				// remove the inputs from the public ledger UTXO pool
				for (Transaction.Input in : tx.getInputs()) {
					UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
					publicLedger.removeUTXO(utxo); 
				}
				// append the outputs to the public ledger UTXO pool
				for (int i= 0; i < tx.numOutputs(); i++) {
					UTXO utxo = new UTXO(tx.getHash(), i);
					publicLedger.addUTXO(utxo, tx.getOutput(i));; 
				}
			}
		}
		Transaction[] validArray= validTx.toArray(new Transaction[validTx.size()]);
		return validArray;
	}

} 
