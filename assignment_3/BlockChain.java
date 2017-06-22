/* test cases for BlockChain
######################
processBlock() tests:
######################
test1():  Process a block with no transactions
test2():  Process a block with a single valid transaction
test3():  Process a block with many valid transactions
test14(): Process a block with some double spends
test14(): Process a new genesis block
test5():  Process a block with an invalid prevBlockHash
test6():  Process blocks with different sorts of invalid transactions
test7():  Process multiple blocks directly on top of the genesis block
test15(): Process a block containing a transaction that claims a UTXO already claimed by a transaction in its parent
test16(): Process a block containing a transaction that claims a UTXO not on its branch
test17(): Process a block containing a transaction that claims a UTXO from earlier in its branch that has not yet been claimed
test8():  Process a linear chain of blocks
test9():  Process a linear chain of blocks of length CUT_OFF_AGE and then a block on top of the genesis block
test10(): Process a linear chain of blocks of length CUT_OFF_AGE + 1 and then a block on top of the genesis block
######################
createBlock() tests:
######################
test11(): Create a block when no transactions have been processed
test12(): Create a block after a single valid transaction has been processed
test13(): Create a block after only invalid transactions have been processed
test22(): Create a block after a valid transaction has been processed, then create a second block
test19(): Create a block after a valid transaction has been processed that is already in a block in the longest valid branch
test20(): Create a block after a valid transaction has been processed that uses a UTXO already claimed by a transaction in the longest valid branch
test21(): Create a block after a valid transaction has been processed that is not a double spend on the longest valid branch and has not yet been included in any other block
######################
Combination tests:
######################
test23(): Process a transaction, create a block, process a transaction, create a block, ...
test24(): Process a transaction, create a block, then process a block on top of that block with a transaction claiming a UTXO from that transaction
test25(): Process a transaction, create a block, then process a block on top of the genesis block with a transaction claiming a UTXO from that transaction
test18(): Process multiple blocks directly on top of the genesis block, then create a block
test26(): Construct two branches of approximately equal size, ensuring that blocks are always created on the proper branch
test27(): Similar to previous test, but then try to process blocks whose parents are at height < maxHeight - CUT_OFF_AGE

 */
import java.util.ArrayList;
import java.util.HashMap;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {

	public static final int CUT_OFF_AGE = 10;
	 
	// all information required in handling a block in block chain
   private class BlockNode {
      public Block block;
      public BlockNode parent;
      public ArrayList<BlockNode> children;
      public int height;
      // the snapshot of utxo pool for making a new block on top of this block
      private UTXOPool uPool;

      public BlockNode(Block block, BlockNode parent, UTXOPool uPool) {
         this.block = block;
         this.parent = parent;
         children = new ArrayList<BlockNode>();
         this.uPool = uPool;
         if (parent != null) {
            height = parent.height + 1;
            parent.children.add(this);
         } else {
            height = 1;
         }
      }

      public UTXOPool getUTXOPoolCopy() {
         return new UTXOPool(uPool);
      }
   }

   private HashMap<ByteArrayWrapper, BlockNode> blockChain;
   private int height;               // height of blockChain
   private BlockNode maxHeightNode;  // top block node in current blockChain.
   private TransactionPool txPool;   // the transactions not processed in current blockChain

   /* create an empty block chain with just a genesis block.
    * Assume genesis block is a valid block
    */
   public BlockChain(Block genesisBlock) {
	   blockChain = new HashMap<ByteArrayWrapper, BlockNode>(); // create an empty blockchain
	   txPool = new TransactionPool();  // create an empty transaction pool
	   UTXOPool utxoPool = new UTXOPool();  // create an empty UTXO pool

	   // add the coinbase transaction to utxo pool
	   Transaction coinbase = genesisBlock.getCoinbase();
	   UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);
	   utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));
	   	   
	   // append the block to block chain
	   BlockNode genesis = new BlockNode(genesisBlock, null, utxoPool);
	   blockChain.put(new ByteArrayWrapper(genesisBlock.getHash()), genesis);
	   height = 1;
	   // set maxHeightNode 
	   maxHeightNode = genesis;
	   
   }

   /* Get the maximum height block
    */
   public Block getMaxHeightBlock() {
	   return maxHeightNode.block;
   }
   
   /* Get the UTXOPool for mining a new block on top of 
    * max height block
    */
   public UTXOPool getMaxHeightUTXOPool() {
	   return maxHeightNode.getUTXOPoolCopy();
   }
   
   /* Get the transaction pool to mine a new block
    */
   public TransactionPool getTransactionPool() {
	   return txPool;
   }

   /* Add a block to block chain if it is valid.
    * For validity, all transactions should be valid
    * and block should be at height > (maxHeight - CUT_OFF_AGE).
    * For example, you can try creating a new block over genesis block 
    * (block height 2) if blockChain height is <= CUT_OFF_AGE + 1. 
    * As soon as height > CUT_OFF_AGE + 1, you cannot create a new block at height 2.
    * Return true of block is successfully added
    */
   public boolean addBlock(Block block) {
	   // the hash of parent node cannot be null
       byte[] parentBlockHash = block.getPrevBlockHash();
       if (parentBlockHash == null)
           return false;
	   // the parent node must be exist in the block chain
       BlockNode parentBlockNode = blockChain.get(new ByteArrayWrapper(parentBlockHash));
       if (parentBlockNode == null) {
           return false;
       }
	   
	   // check all TXs in the block should be valid
       UTXOPool parentUTXOPool = parentBlockNode.getUTXOPoolCopy();
       TxHandler handler = new TxHandler(parentUTXOPool);
       Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);
       // select valid TXs and utxo pool is modified accordingly
       Transaction[] validTxs = handler.handleTxs(txs);
       if (validTxs.length != txs.length) {
           return false;
       }
	   // add the output of the coinbase transaction to utxo pool
       UTXOPool utxoPool = handler.getUTXOPool();
	   Transaction coinbase = block.getCoinbase();
	   UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);
	   utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));
	   
       // Append current block to the block chain and update the height of blockChain
       BlockNode currentNode = new BlockNode(block, parentBlockNode, utxoPool);
       blockChain.put(new ByteArrayWrapper(block.getHash()), currentNode);
       if (currentNode.height > height) {
    	   maxHeightNode = currentNode;
           height = currentNode.height;
        }
/*       
       // update the transaction pool, removing transactions used by block and return it
       ArrayList<Transaction> allTxs = block.getTransactions();
       for (Transaction tx : allTxs) {
           txPool.removeTransaction(tx.getHash());
       }
*/           
       // block should be at height > (maxHeight - CUT_OFF_AGE).
       int proposedHeight = parentBlockNode.height + 1;
       if (proposedHeight <= maxHeightNode.height - CUT_OFF_AGE) {
    	   return false;
       }

	   return true;
   }

   /* Add a transaction in transaction pool in current blockChain
    */
   public void addTransaction(Transaction tx) {
 	  txPool.addTransaction(tx);
   }
}
