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
    private Map blockchain;
    private TransactionPool transactionPool;
    private Node tail; // the max height block of the block chain

    /**
     * create an empty block chain with just a genesis block.
     * Assume {@code genesisBlock} is a valid block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        blockchain = Collections.synchronizedMap(new HashMap<ByteArrayWrapper, Node>());
        UTXOPool utxoPool = new UTXOPool();
        Node genesis = new Node(null, genesisBlock, utxoPool);
        blockchain.put(new ByteArrayWrapper(genesisBlock.getHash()), genesis);
        transactionPool = new TransactionPool();
        updateUTXOPool(genesisBlock, utxoPool);
        tail = genesis;
    }

    /**
     * Assume for simplicity that a coinbase transaction of a block is available
     * to be spent in the next block mined on top of it
     * (This is contrary to the actual Bitcoin protocol when
     * there is a MATURITY period of 100 confirmations before it can be spent).
     * */
    private void updateUTXOPool(Block block, UTXOPool utxoPool) {
        int m=0;
        Transaction trx = block.getCoinbase();
        for (Transaction.Output output: trx.getOutputs()) {
            UTXO newUTXO = new UTXO(trx.getHash(), m);
            utxoPool.addUTXO(newUTXO, output);
            m++;
        }
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return tail.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return tail.getUTXOPool();
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
        // IMPLEMENT THIS/
        if (null == block.getPrevBlockHash()) { // genesis block (parents is a null hash)
            return false;
        }
        Node parent = (Node)blockchain.get(new ByteArrayWrapper(block.getPrevBlockHash()));
        if (null == parent) { // verify if a block has an invalid prevBlockHash
            return false;
        }
        if (blockHasInvalidTrx(block)) {
            return false;
        }
        if (isHeightInvalid(block)) {
            return false;
        }

        updateUTXOPool(block, parent.getUTXOPool());
        Node newNode = new Node(parent, block, parent.getUTXOPool());

        synchronized (blockchain) {
            blockchain.put(new ByteArrayWrapper(block.getHash()), newNode);
        }
        /*for (Transaction transaction: block.getTransactions()) {
            if (null!=transactionPool.getTransaction(transaction.getHash()))
                transactionPool.removeTransaction(transaction.getHash());
        }*/
        if (newNode.height > tail.height)
            tail = newNode;
        return true;
    }

    private boolean isHeightInvalid(Block block) {
        Node parent = (Node)blockchain.get(new ByteArrayWrapper(block.getPrevBlockHash()));
        return (parent.height + 1 <= tail.height - CUT_OFF_AGE);
    }

    private boolean blockHasInvalidTrx(Block block) {
        Node parent = (Node)blockchain.get(new ByteArrayWrapper(block.getPrevBlockHash()));
        TxHandler txHandler = new TxHandler(parent.getUTXOPool());
        Transaction[] trxs = block.getTransactions().toArray(new Transaction[0]);
        return (txHandler.handleTxs(trxs).length!=trxs.length);
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
    }

    /**
     * a node in the block chain
     * */
    private class Node {
        private Node parent;
        private List<Node> children;
        private Block block;
        private UTXOPool utxoPool;
        private int height;
        public Node(Node parent, Block block, UTXOPool utxoPool) {
            this.parent = parent;
            this.children = new ArrayList<>();
            this.block = block;
            this.utxoPool = utxoPool;
            if (null==parent) {
                height = 1;
            } else {
                height = 1 + parent.height;
                parent.children.add(this);
            }
        }
        public UTXOPool getUTXOPool(){
            return this.utxoPool;
        }
    }
}
