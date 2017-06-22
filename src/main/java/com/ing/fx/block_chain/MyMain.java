package com.ing.fx.block_chain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Created by m05b372 on 22-6-2017.
 */
public class MyMain {
    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException {
        KeyPair pk_ken = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_a = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_b = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_c = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        System.out.println(pk_ken.getPublic().hashCode());
        Block genesis = new Block(null, pk_ken.getPublic());
        genesis.finalize();
        BlockChain blockChain = new BlockChain(genesis);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Transaction t1 = new Transaction();
        t1.addInput(genesis.getCoinbase().getHash(), 0);
        t1.addOutput(5, pk_a.getPublic());
        t1.addOutput(10,pk_b.getPublic());
        t1.addOutput(10,pk_c.getPublic());
        t1.signTx(pk_ken.getPrivate(),0);

        // Process a transaction
        blockHandler.processTx(t1);
        // create a block
        Block b1 = blockHandler.createBlock(pk_ken.getPublic());

        if (b1!=null) {
            blockHandler.processBlock(b1);
        }
    }
}
