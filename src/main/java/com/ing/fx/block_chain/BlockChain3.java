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
        blockchain = Collections.synchronizedSet(new LinkedHashSet<Block>(10)); // store item in the order of insertion
        blockchain.add(genesisBlock);
        head = genesisBlock;
        for (Transaction trx: genesisBlock.getTransactions()) {
            updateUTXOPoolAfterMining(trx);
        }
    }

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
        } else if (null!=getBlock(block.getPrevBlockHash())) { // block created by this node, already verified via BlockHandle.createBlock()
            blockchain.add(block);
        } else { // block needs verifying
            if(isNotValid(block)) return false;
            else {
                blockchain.add(block);
            }
        }
        // Update the UTXOPool of the blockchain
        for (Transaction transaction: block.getTransactions()) {
            updateUTXOPoolAfterMining(transaction);
            transactionPool.removeTransaction(transaction.getHash());
        }
        return true;
    }

    /**
     * validate a block not created by this node
     * */
    private boolean isNotValid(Block block) {
        for (Transaction trx: block.getTransactions()) {
            if (!txHandler.isValidTx(trx)) {
                return false;
            }
        }
        return true;
    }

    /**
     * After confirming the transaction (mining), inputs are now officially spent
     * so needs to be removed from the unspent spool
     * outputs are now officially the new unspent so needs
     * to be added to the unspent pool
     * */
    private void updateUTXOPoolAfterMining(Transaction validTx) {
        int m=0;
        for (Transaction.Output output: validTx.getOutputs()) {
            UTXO newUTXO = new UTXO(validTx.getHash(), m);
            txHandler.getUTXOPool().addUTXO(newUTXO, output);
        }
        for (Transaction.Input input: validTx.getInputs()) {
            UTXO spentUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            for (UTXO utxo: txHandler.getUTXOPool().getAllUTXO()){
                if (utxo.compareTo(spentUTXO)==0)
                    txHandler.getUTXOPool().removeUTXO(utxo);
            }
        }
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        if (null==transactionPool.getTransaction(tx.getHash())) {
            transactionPool.addTransaction(tx);
        }
    }
}
