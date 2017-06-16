package com.ing.fx.block_chain;

import com.ing.fx.scrooge_coin.TxHandler;
import com.ing.fx.scrooge_coin.UTXO;
import com.ing.fx.scrooge_coin.UTXOPool;

import java.util.*;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.

/**
 * The BlockChain class is responsible for maintaining a block chain.
 * Since the entire block chain could be huge in size,
 * you should only keep around the most recent blocks.
 * The exact number to store is up to your design, as long as you're able to implement all the API functions.
 * */
public class BlockChain2 {
    /* nek: A block chain is just like a regular linked list having a series of block
    * Each block has data and a pointer to the previous block in this list.
    * The only thing is that the block pointer is a hash pointer
    * As a hash value, the hash pointer therefore can not only say where the previous block is
    * but also can say what the value of the entire previous block is.
    * Properties: tamper-evident, we only have to remember the head of the list to make sure
    * that the whole chain has not been tampered.
    *
    * The Merkel tree: a binary search tree (instead of a linked list) providing an advantage of verifying if
    * something is legitimate without scanning the whole population (just a branch!
    * which has a computational complexity of log n to verify)
    *
    * Hash pointer is effective for any pointer-based data structure as long as
    * the data structure is not cyclic like directed acyclic graph
    *
    * Bitcoin use both hash-based data structure.
    * Linked list for the block chain and
    * Merkel tree for transactions in each block.
    * */
    public static final int CUT_OFF_AGE = 10;

    private TransactionPool transactionPool = new TransactionPool();

    private Set<Block> blockPool; // all the most recent blocks in the chain

    private Block maxHeightBlock;
    private int maxHeight;

    private UTXOPool uTXOPool_maxHeight = new UTXOPool();

    private Block genesis;

    private TxHandler txHandler = new TxHandler(new UTXOPool());

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain2(Block genesisBlock) {
        // IMPLEMENT THIS
        genesis = genesisBlock;
        maxHeightBlock = genesisBlock;
        blockPool = Collections.synchronizedSet(new LinkedHashSet<Block>(10)); // store item in the order of insertion
        blockPool.add(genesisBlock);
    }

    /** Get the maximum height block */

    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS

        try {
            // If there are multiple blocks at the same height, return the oldest block
            Iterator<Block> itr = blockPool.iterator();
            while (itr.hasNext()) {
                Block block = itr.next();
                int temp = howTall(block);
                if (temp >= maxHeight) {
                    maxHeight = temp;
                    maxHeightBlock = block; // the oldest block with max height
                }
            }
        } catch (Exception e) {
            System.out.println("Error at getMaxHeightBlock(): " + e.getMessage());
        }
        return maxHeightBlock;
    }

    private int howTall(Block block) {
        if (null==block.getPrevBlockHash()) return 0; // genesis
        else return (1 + howTall(getPrevBlock(block)));
    }

    private Block getPrevBlock(Block block) {
        Iterator<Block> itr = blockPool.iterator();
        while(itr.hasNext()) {
            Block blk = itr.next();
            if (null!=blk.getHash()) {
                if (blk.getHash().equals(block.getPrevBlockHash())) return blk;
            }
        }
        return null; // genesis block
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return uTXOPool_maxHeight;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS

        try {
            // A new genesis block wont be mined.
            // If you receive a block which claims to be a genesis block (parent is a null hash)
            // in the addBlock(Block b) function, you can return false.
            if (null == block.getPrevBlockHash()) {
                return false;
            } else {
                if (blockPool.size() > 10) renewBlockChain();
                if (maxHeight <= CUT_OFF_AGE + 1) {
                    if (!isHashCorrect(block)) return false; // hash not correct!
                    for (Transaction trx : block.getTransactions()) {
                        if (!txHandler.isValidTx(trx)) return false; // all transactions should be valid!
                    }
                    /*if (howTall(block) > maxHeight - CUT_OFF_AGE)
                        return false; // block builds on current longest chain to avoid forks*/
                    registerUTXO(block);
                    blockPool.add(block);
                    updateMaxHeightUTXO(block);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("Error at addBlock(): " + e.getMessage());
        }
        return false;
    }

    private boolean isHashCorrect(Block block) {
        return null!=block.getHash();
    }

    /**
     * update all the pools that used by block chain to create new block
     * */
    private void updateMaxHeightUTXO(Block block) {
        for (Transaction tx: block.getTransactions()) {
            int i = 0;
            for (Transaction.Output output: tx.getOutputs()) {
                UTXO utxo = new UTXO(tx.getHash(), i);
                i++;
                if(uTXOPool_maxHeight.contains(utxo)) {
                    uTXOPool_maxHeight.removeUTXO(utxo);
                }
            }
            if (null!=transactionPool.getTransaction(tx.getHash()))
                transactionPool.removeTransaction(tx.getHash());
        }
    }

    /**
     * Since there can be (multiple) forks, blocks form a tree rather than a list.
     * Your design should take this into account. You have to maintain a UTXO pool
     * corresponding to every block on top of which a new block might be created.
     */
    private void registerUTXO(Block block) {
        for (Transaction trx: block.getTransactions()) {
            for (Transaction.Output output: trx.getOutputs()) {
                int j = 0;
                UTXO registeredUTXO = new UTXO(trx.getHash(), j);
                if (null!=txHandler.getUTXOPool()) {
                    if (!txHandler.getUTXOPool().contains(registeredUTXO)) {
                        txHandler.getUTXOPool().addUTXO(registeredUTXO, output);
                    }
                }
            }
        }
    }

    private void renewBlockChain() {
        blockPool = Collections.synchronizedSet(new LinkedHashSet<Block>(10)); // remove all;
        blockPool.add(genesis);
        maxHeightBlock = genesis;
        maxHeight = 1;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        try {
            if (null!=tx.getHash()) {
                if (null==transactionPool.getTransaction(tx.getHash())) { // have not heard of this transaction
                    if (txHandler.isValidTx(tx)) { // tx valid with current block chain, avoid double-spends
                        transactionPool.addTransaction(tx); // add it to the pool!
                        int i = 0;
                        for (Transaction.Output output : tx.getOutputs()) {
                            UTXO utxo = new UTXO(tx.getHash(), i);
                            if (!uTXOPool_maxHeight.contains(utxo)) {
                                uTXOPool_maxHeight.addUTXO(utxo, output);
                            }
                            if (!txHandler.getUTXOPool().contains(utxo)) {
                                txHandler.getUTXOPool().addUTXO(utxo, output);
                            }
                            i++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error at addTransaction(): " + e.getMessage());
        }
    }
}