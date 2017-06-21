package com.ing.fx.block_chain;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Bitcoin is transaction based ledger, not account-based because there is no separate data structure to store the balance
 * the balance data is embedded inside the block chain itself!
 * */

import com.ing.fx.scrooge_coin.UTXO;
/*
 * There are two types of scrooge transaction: createdCoin and payCoin
 * for paid ones: total output values = total input value
 * The trx has to be signed by everyone who's paying in
 */
/**
 * represents a ScroogeCoin transaction and has inner classes Transaction.Output and Transaction.Input.
 * */
public class Transaction {

    /**
     * A transaction input consists of the hash of the transaction that contains the corresponding output,
     * the index of this output in that transaction (indices are simply integers starting from 0),
     * and a digital signature.
     * For the input to be valid, the signature it contains must be a valid signature
     * over the current transaction with the public key in the spent output.
     * */
    public class Input {
        /** hash of the Transaction whose output is being used */
        public byte[] prevTxHash;
        /** used output's index in the previous transaction */
        public int outputIndex;
        /** the signature produced to check validity */
        public byte[] signature; // in reality, this is also a script called SigScript

        public Input(byte[] prevHash, int index) {
            if (prevHash == null)
                prevTxHash = null;
            else
                prevTxHash = Arrays.copyOf(prevHash, prevHash.length);
            outputIndex = index;
        }

        public void addSignature(byte[] sig) {
            if (sig == null)
                signature = null;
            else
                signature = Arrays.copyOf(sig, sig.length);
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }

            Input in = (Input) other;

            if (prevTxHash.length != in.prevTxHash.length)
                return false;
            for (int i = 0; i < prevTxHash.length; i++) {
                if (prevTxHash[i] != in.prevTxHash[i])
                    return false;
            }
            if (outputIndex != in.outputIndex)
                return false;
            if (signature.length != in.signature.length)
                return false;
            for (int i = 0; i < signature.length; i++) {
                if (signature[i] != in.signature[i])
                    return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 1;
            hash = hash * 17 + Arrays.hashCode(prevTxHash);
            hash = hash * 31 + outputIndex;
            hash = hash * 31 + Arrays.hashCode(signature);
            return hash;
        }
    }

    /**
     * A transaction output consists of a value and a public key to which it is being paid.
     * For the public keys, we use the built-in Java PublicKey class.
     * These are coins to be created from the consumed coins (Input)
     * */
    public class Output {
        /** value in bitcoins of the output */
        public double value;
        /** the address or public key of the recipient */ // in Bitcoin, there is actually a Bitcoin script here
        public PublicKey address; // In reality, this is Bitcoin script called PubScript.
        /** SigScript and PubScript will get pasted together when a transaction is validated
         * and if the concatenated script can run without errors, this is considered a valid transaction.
         * More useful info in Bitcoin Script and Application of Bitcoin Scripts
         * */

        public Output(double v, PublicKey addr) {
            value = v;
            address = addr;
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }

            Output op = (Output) other;

            if (value != op.value)
                return false;
            if (!((RSAPublicKey) address).getPublicExponent().equals(
                    ((RSAPublicKey) op.address).getPublicExponent()))
                return false;
            if (!((RSAPublicKey) address).getModulus().equals(
                    ((RSAPublicKey) op.address).getModulus()))
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 1;
            hash = hash * 17 + (int) value * 10000;
            hash = hash * 31 + ((RSAPublicKey) address).getPublicExponent().hashCode();
            hash = hash * 31 + ((RSAPublicKey) address).getModulus().hashCode();
            return hash;
        }
    }

    /** hash of the transaction, its unique id
     * to let us do hash pointer
     * */
    private byte[] hash;
    private ArrayList<Input> inputs;
    private ArrayList<Output> outputs;
    private boolean coinbase; // indicate if this is coinbase or normal transaction.

    public Transaction() {
        inputs = new ArrayList<Input>();
        outputs = new ArrayList<Output>();
        coinbase = false;
    }

    public Transaction(Transaction tx) {
        hash = tx.hash.clone();
        inputs = new ArrayList<Input>(tx.inputs);
        outputs = new ArrayList<Output>(tx.outputs);
        coinbase = false;
    }

    /** create a coinbase transaction of value {@code coin} and calls finalize on it */
    public Transaction(double coin, PublicKey address) {
        coinbase = true;
        inputs = new ArrayList<Input>();
        outputs = new ArrayList<Output>();
        addOutput(coin, address);
        finalize(); // hash of coin base transaction is set here in finalize()
    }

    public boolean isCoinbase() {
        return coinbase;
    }

    public void addInput(byte[] prevTxHash, int outputIndex) {
        Input in = new Input(prevTxHash, outputIndex);
        inputs.add(in);
    }

    public void addOutput(double value, PublicKey address) {
        Output op = new Output(value, address);
        outputs.add(op);
    }

    public void removeInput(int index) {
        inputs.remove(index);
    }

    public void removeInput(UTXO ut) {
        for (int i = 0; i < inputs.size(); i++) {
            Input in = inputs.get(i);
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
            if (u.equals(ut)) {
                inputs.remove(i);
                return;
            }
        }
    }

    /** nek
     * Digital signature API has the following scheme:
     * - Generates two keys of the signer:
     * (sk, pk) = generateKeys(keysize)
     * - The signer signs her message using the secret signing key sk:
     * signature = sign(secret_key, message)
     * - To verify if a signature is valid, use the public key and the message:
     * isValid = verify(pk, message, signature)
     * */
    public byte[] getRawDataToSign(int index) {
        // ith input and all outputs
        ArrayList<Byte> sigData = new ArrayList<Byte>();
        if (index > inputs.size())
            return null;
        Input in = inputs.get(index);
        byte[] prevTxHash = in.prevTxHash;
        ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / 8);
        b.putInt(in.outputIndex);
        byte[] outputIndex = b.array();
        if (prevTxHash != null)
            for (int i = 0; i < prevTxHash.length; i++)
                sigData.add(prevTxHash[i]);
        for (int i = 0; i < outputIndex.length; i++)
            sigData.add(outputIndex[i]);
        for (Output op : outputs) {
            ByteBuffer bo = ByteBuffer.allocate(Double.SIZE / 8);
            bo.putDouble(op.value);
            byte[] value = bo.array();
            byte[] addressExponent = ((RSAPublicKey) op.address).getPublicExponent().toByteArray();
            byte[] addressModulus = ((RSAPublicKey) op.address).getModulus().toByteArray();
            for (int i = 0; i < value.length; i++)
                sigData.add(value[i]);
            for (int i = 0; i < addressExponent.length; i++)
                sigData.add(addressExponent[i]);
            for (int i = 0; i < addressModulus.length; i++)
                sigData.add(addressModulus[i]);
        }
        byte[] sigD = new byte[sigData.size()];
        int i = 0;
        for (Byte sb : sigData)
            sigD[i++] = sb;
        return sigD;
    }

    public void addSignature(byte[] signature, int index) {
        inputs.get(index).addSignature(signature);
    }

    /**
     * A transaction consists of a list of inputs, a list of outputs and a unique ID
     * */
    public byte[] getRawTx() {
        ArrayList<Byte> rawTx = new ArrayList<Byte>();
        for (Input in : inputs) {
            byte[] prevTxHash = in.prevTxHash;
            ByteBuffer b = ByteBuffer.allocate(Integer.SIZE / 8);
            b.putInt(in.outputIndex);
            byte[] outputIndex = b.array();
            byte[] signature = in.signature;
            if (prevTxHash != null)
                for (int i = 0; i < prevTxHash.length; i++)
                    rawTx.add(prevTxHash[i]);
            for (int i = 0; i < outputIndex.length; i++)
                rawTx.add(outputIndex[i]);
            if (signature != null)
                for (int i = 0; i < signature.length; i++)
                    rawTx.add(signature[i]);
        }
        for (Output op : outputs) {
            ByteBuffer b = ByteBuffer.allocate(Double.SIZE / 8);
            b.putDouble(op.value);
            byte[] value = b.array();
            byte[] addressExponent = ((RSAPublicKey) op.address).getPublicExponent().toByteArray();
            byte[] addressModulus = ((RSAPublicKey) op.address).getModulus().toByteArray();
            for (int i = 0; i < value.length; i++)
                rawTx.add(value[i]);
            for (int i = 0; i < addressExponent.length; i++)
                rawTx.add(addressExponent[i]);
            for (int i = 0; i < addressModulus.length; i++)
                rawTx.add(addressModulus[i]);
        }
        byte[] tx = new byte[rawTx.size()];
        int i = 0;
        for (Byte b : rawTx)
            tx[i++] = b;
        return tx;
    }

    public void finalize() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(getRawTx());
            hash = md.digest(); // nek: padding in SHA-256 means that the length of the message is exactly a multiple of the 512 bit block size!
        } catch (NoSuchAlgorithmException x) {
            x.printStackTrace(System.err);
        }
    }

    public void setHash(byte[] h) {
        hash = h;
    }

    public byte[] getHash() {
        return hash;
    }

    public ArrayList<Input> getInputs() {
        return inputs;
    }

    public ArrayList<Output> getOutputs() {
        return outputs;
    }

    public Input getInput(int index) {
        if (index < inputs.size()) {
            return inputs.get(index);
        }
        return null;
    }

    public Output getOutput(int index) {
        if (index < outputs.size()) {
            return outputs.get(index);
        }
        return null;
    }

    public int numInputs() {
        return inputs.size();
    }

    public int numOutputs() {
        return outputs.size();
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        Transaction tx = (Transaction) other;
        // inputs and outputs should be same
        if (tx.numInputs() != numInputs())
            return false;

        for (int i = 0; i < numInputs(); i++) {
            if (!getInput(i).equals(tx.getInput(i)))
                return false;
        }

        if (tx.numOutputs() != numOutputs())
            return false;

        for (int i = 0; i < numOutputs(); i++) {
            if (!getOutput(i).equals(tx.getOutput(i)))
                return false;
        }
        return true;
    }

    /**
     * each trx has an unique id which is the hash
     * */
    public int hashCode() {
        int hash = 1;
        for (int i = 0; i < numInputs(); i++) {
            hash = hash * 31 + getInput(i).hashCode();
        }
        for (int i = 0; i < numOutputs(); i++) {
            hash = hash * 31 + getOutput(i).hashCode();
        }
        return hash;
    }

    public void signTx(PrivateKey sk, int input) throws SignatureException {
        Signature sig = null;
        try {
            sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(sk);
            sig.update(this.getRawDataToSign(input));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        this.addSignature(sig.sign(),input);
        // Note that this method is incorrectly named, and should not in fact override the Java
        // object finalize garbage collection related method.
        this.finalize();
    }
}
