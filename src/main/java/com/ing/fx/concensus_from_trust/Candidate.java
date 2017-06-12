package com.ing.fx.concensus_from_trust;

// a simple class describing candidate transactions your node receives
public class Candidate {
    Transaction tx;
    int sender;

    // transaction tx broadcast from node[sender]
    public Candidate(Transaction tx, int sender) {
        this.tx = tx;
        this.sender = sender;
    }
}

