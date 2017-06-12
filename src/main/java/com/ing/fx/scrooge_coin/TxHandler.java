package com.ing.fx.scrooge_coin;
/** Scrooge coin is supposed to be the same as Goofy coin but with the ability to avoid double-spending
 * Scrooge publishes a history of all transactions that have happened (a block chain, signed by Scrooge)
 * Each block has a trx in it and a hash pointer to the previous block in the history (In bitcoins, we put
 * multiple trx into a block!) The history will help us to detect double spending.
 * */
import java.math.BigDecimal;
import java.util.ArrayList;
import com.ing.fx.block_chain.Transaction;
/**
 * Q: you have ten coins each of value 3.0
 * You would like to transfer coins of value 5.0 to your friend. This requires:
 * One transaction, two new coins created, and two signatures
 * because: you will need to consume two coins of value 3.0 to produce two new coins
 * one of 5.0 and the other of 1.0
 * Since you consumed two coins of value 3.0, you need signature of 2 previous owner of these coins in this transaction.
 * */

public class TxHandler {
    private UTXOPool currentUTXOPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        currentUTXOPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, nek: inputs are all coins to be consumed to produce new outputs
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        /**
         * trx ID: xx
         * type: PayCoins
         * consumed coins: x1, x2, x3 ... (these are inputs which has corresponding output from previous transactions)
         * coins created: x4, x5 ... (these are outputs)
         * signatures of all the owners of the consumed coins signing on this transaction sig=sign(sk, trx)
         * */
        // IMPLEMENT THIS
        BigDecimal totalOutput = new BigDecimal(0);
        for (Transaction.Output output: tx.getOutputs()) {
            if (output.value < 0) return false; //(4)
            else totalOutput = totalOutput.add(new BigDecimal(output.value));
        }
        BigDecimal totalInput = new BigDecimal(0);
        ArrayList<Transaction.Output> claimedUTXOs = new ArrayList<>();
        int i = 0;
        for (Transaction.Input input: tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex); //reconstructing corresponding output from previous trx
            if (utxo==null) return false;
            else {
                if (currentUTXOPool.getTxOutput(utxo)==null) //(1)
                    return false;
                else {
                    Transaction.Output claimedOutput = currentUTXOPool.getTxOutput(utxo);
                    if (claimedUTXOs.contains(claimedOutput)) //(3)
                        return false;
                    else {
                        claimedUTXOs.add(claimedOutput);
                    }
                    totalInput = totalInput.add(new BigDecimal(claimedOutput.value));
                    if (!Crypto.verifySignature(claimedOutput.address, tx.getRawDataToSign(i), input.signature)) return false; //(2) verify(pk, trx, sig)
                }
            }
            i++;
        }

        if (totalOutput.compareTo(totalInput) == 1) return false; //(5)

        return true; //Test 1: test isValidTx() with valid transactions
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Transaction[] result = new Transaction[possibleTxs.length];
        int k=0;
        for (int j=0; j<possibleTxs.length; j++) {
            if (isValidTx(possibleTxs[j])) {
                result[k] = possibleTxs[j];
                k++;
                updateCurrentUTXOPool(possibleTxs[j]);
            }
        }
        return result;
    }

    /**
     * Based on the transactions it has chosen to accept,
     * handleTxs() should also update its internal UTXOPool to reflect the current set of unspent transaction outputs,
     * so that future calls to handleTxs() and isValidTx() are able to correctly process/validate transactions
     * that claim outputs from transactions that were accepted in a previous call to handleTxs().
     * */
    private void updateCurrentUTXOPool(Transaction validTx) {
        int m=0;
        for (Transaction.Output output: validTx.getOutputs()) {
            UTXO newUTXO = new UTXO(validTx.getHash(), m);
            this.currentUTXOPool.addUTXO(newUTXO, output);
        }
        for (Transaction.Input input: validTx.getInputs()) {
            UTXO textUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            for (UTXO utxo: this.currentUTXOPool.getAllUTXO()){
                if (utxo.compareTo(textUTXO)==0)
                    this.currentUTXOPool.removeUTXO(utxo);
            }
        }
    }

}
