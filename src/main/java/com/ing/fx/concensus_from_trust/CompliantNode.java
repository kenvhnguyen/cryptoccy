package com.ing.fx.concensus_from_trust;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
        // what is the purpose of this??
    }

    private List<Integer> followees_list = new ArrayList<Integer>();
    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        for (int j=0; j<followees.length; j++) {
            if (followees[j]) {
                followees_list.add(j); // this node follows node j, adding j to the list of followees
            }
        }
        // what is the purpose of this??
    }

    private Set<Transaction> proposalTransactions = new HashSet<Transaction>();
    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        for(Transaction tx: pendingTransactions) {
            proposalTransactions.add(tx); // your initial proposed transactions
        }
    }

    public Set<Transaction> sendToFollowers() {
        return proposalTransactions; // continue to broadcast your list of proposed transactions
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for (Candidate candidate: candidates) {
            if (!proposalTransactions.contains(candidate.tx) ) { // new transaction coming from your trusted peer
                this.proposalTransactions.add(candidate.tx); // need to be added to your agreed list of transactions
            }
        }
    }
}
