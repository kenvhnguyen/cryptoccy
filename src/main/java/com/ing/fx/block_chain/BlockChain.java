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
     * Since there can be (multiple) forks, blocks form a tree rather than a list.
     * Your design should take this into account. You have to maintain a UTXO pool
     * corresponding to every block on top of which a new block might be created.
     * */
    private TxHandler txHandler = new TxHandler(new UTXOPool());

    private TransactionPool transactionPool = new TransactionPool();

    private Block head; // the max height block of the block chain

    private int height; // the max height of the block chain

    /**
     * create an empty block chain with just a genesis block.
     * Assume {@code genesisBlock} is a valid block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        // use LinkedHashSet to store item in the order of insertion
        blockchain = Collections.synchronizedSet(new LinkedHashSet<Block>());
        blockchain.add(genesisBlock);
        head = genesisBlock;
        for (Transaction trx: genesisBlock.getTransactions()) {
            updateUTXOPool(trx);
        }
    }

    private void updateUTXOPool(Transaction validTx) {
        int m=0;
        for (Transaction.Output output: validTx.getOutputs()) {
            UTXO newUTXO = new UTXO(validTx.getHash(), m);
            txHandler.getUTXOPool().addUTXO(newUTXO, output);
        }
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        for (Block block : blockchain) {
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
        for (Block block : blockchain) {
            if (block.getHash().equals(hash)) return block;
        }
        return null;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return txHandler.getUTXOPool();
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
        if (null == block.getPrevBlockHash()) {
            return false;
        /*} else if (getHeight(block) > (height - CUT_OFF_AGE)) {
            return false;*/
        } else if (null==getBlock(block.getPrevBlockHash())) {
            return false;
        } else if (hasDoubleSpent(block)) {
            return false;
        } else {
            blockchain.add(block);
        }
        // Update the UTXOPool of the blockchain
        /**
         * Maintain only one global Transaction Pool for the block chain and
         * keep adding transactions to it on receiving transactions and
         * remove transactions from it if a new block is received or created
         * It is okay if some transactions get dropped during a block chain reorganization,
         * i.e., when a side branch becomes the new longest branch
         * Specifically, transactions present in the original main branch (and thus removed from the transaction pool)
         * but absent in the side branch might get lost.
         * */
        for (Transaction transaction: block.getTransactions()) {
            if (null!=transactionPool.getTransaction(transaction.getHash()))
                transactionPool.removeTransaction(transaction.getHash());
        }
        /**
         * Assume for simplicity that a coinbase transaction of a block is available
         * to be spent in the next block mined on top of it
         * (This is contrary to the actual Bitcoin protocol when
         * there is a MATURITY period of 100 confirmations before it can be spent).
         * */
        if (null!=block.getCoinbase()) {
            transactionPool.addTransaction(block.getCoinbase());
            updateUTXOPool(block.getCoinbase());
        }

        return true;
    }

    private ArrayList<UTXO> claimedUTXO;
    private boolean hasDoubleSpent(Block block) {
        claimedUTXO = new ArrayList<>();
        for (Transaction transaction: block.getTransactions()) {
            for (Transaction.Input input: transaction.getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                if (claimedUTXO.contains(utxo))
                    return true; // double spent found!
                else
                    claimedUTXO.add(utxo);
            }
        }
        return false;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        if (null==transactionPool.getTransaction(tx.getHash())) {
            transactionPool.addTransaction(tx);
        }
    }
}
