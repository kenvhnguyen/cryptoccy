package com.ing.fx.scrooge_coin;

/**
 * Created by m05b372 on 30-5-2017.
 */

import com.ing.fx.block_chain.*;
import com.ing.fx.block_chain.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Extra Credit: Create a second file called MaxFeeTxHandler.java whose handleTxs() method
 * finds a set of transactions with maximum total transaction fees --
 * i.e. maximize the sum over all transactions in the set of (sum of input values - sum of output values)).
 * */
public class MaxFeeTxHandler {
    private UTXOPool currentUTXOPool;
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        currentUTXOPool = new UTXOPool(utxoPool);
    }

    public boolean isValidTx(com.ing.fx.block_chain.Transaction tx) {
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

    public com.ing.fx.block_chain.Transaction[] handleTxs(com.ing.fx.block_chain.Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        com.ing.fx.block_chain.Transaction[] result = new Transaction[possibleTxs.length];
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
