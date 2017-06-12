package com.ing.fx.block_chain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Stores the block data structure.
 * */
public class Block {

    /**
     * The coinbase value is kept constant at 25 bitcoins
     * whereas in reality it halves roughly every 4 years and is currently 12.5 BTC.
     * */
    public static final double COINBASE = 25;

    private byte[] hash;
    private byte[] prevBlockHash;
    private Transaction coinbase; // each block has this one special trx where the creation of new coins happens
    private ArrayList<Transaction> txs;  // for simplicity, we use just a array instead of Merkel tree to store trx in each block

    /** {@code address} is the address to which the coinbase transaction would go */
    public Block(byte[] prevHash, PublicKey address) {
        prevBlockHash = prevHash;
        coinbase = new Transaction(COINBASE, address);
        txs = new ArrayList<Transaction>();
    }

    public Transaction getCoinbase() {
        return coinbase;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    public ArrayList<Transaction> getTransactions() {
        return txs;
    }

    public Transaction getTransaction(int index) {
        return txs.get(index);
    }

    public void addTransaction(Transaction tx) {
        txs.add(tx);
    }

    public byte[] getRawBlock() {
        ArrayList<Byte> rawBlock = new ArrayList<Byte>();
        if (prevBlockHash != null)
            for (int i = 0; i < prevBlockHash.length; i++)
                rawBlock.add(prevBlockHash[i]);
        for (int i = 0; i < txs.size(); i++) {
            byte[] rawTx = txs.get(i).getRawTx();
            for (int j = 0; j < rawTx.length; j++) {
                rawBlock.add(rawTx[j]);
            }
        }
        byte[] raw = new byte[rawBlock.size()];
        for (int i = 0; i < raw.length; i++)
            raw[i] = rawBlock.get(i);
        return raw;
    }

    public void finalize() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawBlock());
            hash = md.digest();
            /* nek: SHA-256 hash function breaks the message into blocks that are 512 bits in size
            * Since messages will not always a multiple of the block size, padding is needed at the end.
            * Padding is at the end = 1 bit + some number of zero bits + msg length field 64 bits
            * start hashing an IV number (initial vector of 256 bits) and the first block using function c
            * and then repeating hashing the outcome with the following block. If function c is collision-free
            * then the final hashing value will also be collision-free
            * */
        } catch (NoSuchAlgorithmException x) {
            x.printStackTrace(System.err);
        }
    }
}
