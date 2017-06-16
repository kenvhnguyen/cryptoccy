package com.ing.fx.block_chain;

import com.ing.fx.scrooge_coin.TxHandler;
import com.ing.fx.scrooge_coin.UTXO;
import com.ing.fx.scrooge_coin.UTXOPool;

/**
 * Created by m05b372 on 16-6-2017.
 */



import java.util.*;
public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private Set<Block> blockchain; // all the most recent blocks in the chain
    /**
     * create an empty block chain with just a genesis block.
     * Assume {@code genesisBlock} is a valid block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        blockchain = Collections.synchronizedSet(new LinkedHashSet<Block>(10)); // store item in the order of insertion
        blockchain.add(genesisBlock);
        head = genesisBlock;
    }

    private Block head;
    private int height;
    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        Iterator iterator = blockchain.iterator();
        while (iterator.hasNext()) {
            Block block = (Block)iterator.next();
            if (getHeight(block) >= height) {
                height = getHeight(block);
                head = block;
            }
        }
        return head;
    }

    private int getHeight(Block block) {
        if (block==null) return 0;
        else return 1 + getHeight(getBlock(block.getPrevBlockHash()));
    }

    private Block getBlock(byte[] hash) {
        Iterator iterator = blockchain.iterator();
        while(iterator.hasNext()) {
            Block block = (Block)iterator.next();
            if (block.getHash().equals(hash)) return block;
        }
        return null;
    }

    private UTXOPool uTXOPool_maxHeight = new UTXOPool();
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
        if (null!=getBlock(block.getPrevBlockHash())) { // block created by this node, already valid
            blockchain.add(block);
        } else { // block needs verifying
            if(isNotValid(block)) return false;
            else {
                blockchain.add(block);
                for (Transaction transaction: block.getTransactions()) {
                    addTxToUTXOPool(transaction);
                }
            }
        }
        for (Transaction transaction: block.getTransactions()) {
            transactionPool.removeTransaction(transaction.getHash());
        }
        return true;
    }

    private boolean isNotValid(Block block) {
        for (Transaction trx: block.getTransactions()) {
            if (!txHandler.isValidTx(trx)) return false;
        }
        return true;
    }

    private TxHandler txHandler = new TxHandler(new UTXOPool());

    private TransactionPool transactionPool = new TransactionPool();
    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
        addTxToUTXOPool(tx);
    }

    private void addTxToUTXOPool(Transaction tx) {
        int index = 0;
        for (Transaction.Output output: tx.getOutputs()) {
            UTXO utxo = new UTXO(tx.getHash(), index);
            txHandler.getUTXOPool().addUTXO(utxo, output);
            index++;
        }
    }
}
