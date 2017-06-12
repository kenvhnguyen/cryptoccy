package com.ing.fx.block_chain;

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
public class BlockChain {
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

    private Set<Block> blockPool; //nek: all the blocks in the chain

    private Block maxHeightBlock;
    private int maxHeight;

    private UTXOPool uTXOPool = new UTXOPool();

    private Block genesis;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        genesis = genesisBlock;
        blockPool = Collections.synchronizedSet(new LinkedHashSet<Block>(10)); // store item in the order of insertion
        blockPool.add(genesisBlock);
    }

    /** Get the maximum height block */

    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS

        // If there are multiple blocks at the same height, return the oldest block
        Iterator<Block> itr = blockPool.iterator();
        while(itr.hasNext()) {
            Block block = itr.next();
            int temp = howTall(block);
            if (temp >= maxHeight) {
                maxHeight = temp;
                maxHeightBlock = block; // the oldest block with max height
            }
        }

        return maxHeightBlock;
    }

    private int howTall(Block block) {
        if (null==block) return 0; // no more height
        else return (1 + howTall(getPrevBlock(block)));
    }

    private Block getPrevBlock(Block block) {
        Iterator<Block> itr = blockPool.iterator();
        while(itr.hasNext()) {
            Block blk = itr.next();
            if (blk.getHash().equals(block.getPrevBlockHash())) return blk;
        }
        return null; // genesis block
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return uTXOPool;
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

        // A new genesis block wont be mined.
        // If you receive a block which claims to be a genesis block (parent is a null hash)
        // in the addBlock(Block b) function, you can return false.
        if (null == block.getPrevBlockHash()) {
            return false;
        } else {
            if (blockPool.size()>10) renewBlockChain();
            if (maxHeight <= CUT_OFF_AGE + 1) {
                blockPool.add(block);
                for (Transaction trx: block.getTransactions()) {
                    for (Transaction.Output output: trx.getOutputs()) {
                        int j = 0;
                        UTXO registeredUTXO = new UTXO(trx.getHash(), j);
                        if (uTXOPool.contains(registeredUTXO)) {
                            uTXOPool.removeUTXO(registeredUTXO);
                        }
                    }
                }
            } else {
                return false;
            }
            return true;
        }
    }

    private void renewBlockChain() {
        blockPool = Collections.synchronizedSet(new LinkedHashSet<Block>(10)); // remove all;
        blockPool.add(genesis);
        maxHeight = 1;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
        int i = 0;
        for (Transaction.Output output: tx.getOutputs()) {
            UTXO utxo = new UTXO(tx.getHash(), i);
            i++;
            if(!uTXOPool.contains(utxo)) {
                uTXOPool.addUTXO(utxo, output);
            }
        }
    }
}