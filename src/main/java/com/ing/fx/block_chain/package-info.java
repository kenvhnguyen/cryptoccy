/**
 * Created by m05b372 on 9-6-2017.
 */
package com.ing.fx.block_chain;

/**
 * In this assignment you will implement a node that’s part of a block-chain-based distributed consensus protocol.
 * Specifically, your code will receive incoming transactions and blocks and maintain an updated block chain.
  * */

/**
 * Test output:
 * Running 27 tests.

 ######################
 processBlock() tests:
 ######################
 Test 1: Process a block with no transactions
 ==> passed

 Test 2: Process a block with a single valid transaction
 ==> passed

 Test 3: Process a block with many valid transactions
 ==> passed

 Test 4: Process a block with some double spends
 ==> FAILED

 Test 5: Process a new genesis block
 ==> passed

 Test 6: Process a block with an invalid prevBlockHash
 ==> FAILED

 Test 7: Process blocks with different sorts of invalid transactions
 ==> FAILED

 Test 8: Process multiple blocks directly on top of the genesis block
 ==> passed

 Test 9: Process a block containing a transaction that claims a UTXO already claimed by a transaction in its parent
 ==> FAILED

 Test 10: Process a block containing a transaction that claims a UTXO not on its branch
 ==> FAILED

 Test 11: Process a block containing a transaction that claims a UTXO from earlier in its branch that has not yet been claimed
 ==> passed

 Test 12: Process a linear chain of blocks
 ==> passed

 Test 13: Process a linear chain of blocks of length CUT_OFF_AGE and then a block on top of the genesis block
 ==> passed

 Test 14: Process a linear chain of blocks of length CUT_OFF_AGE + 1 and then a block on top of the genesis block
 ==> FAILED

 ######################
 createBlock() tests:
 ######################
 Test 15: Create a block when no transactions have been processed
 ==> passed

 Test 16: Create a block after a single valid transaction has been processed
 ==> FAILED

 Test 17: Create a block after a valid transaction has been processed, then create a second block
 ==> FAILED

 Test 18: Create a block after a valid transaction has been processed that is already in a block in the longest valid branch
 ==> passed

 Test 19: Create a block after a valid transaction has been processed that uses a UTXO already claimed by a transaction in the longest valid branch
 ==> passed

 Test 20: Create a block after a valid transaction has been processed that is not a double spend on the longest valid branch and has not yet been included in any other block
 ==> FAILED

 Test 21: Create a block after only invalid transactions have been processed
 ==> passed

 ######################
 Combination tests:
 ######################
 Test 22: Process a transaction, create a block, process a transaction, create a block, ...
 ==> FAILED

 Test 23: Process a transaction, create a block, then process a block on top of that block with a transaction claiming a UTXO from that transaction
 ==> FAILED

 Test 24: Process a transaction, create a block, then process a block on top of the genesis block with a transaction claiming a UTXO from that transaction
 ==> FAILED

 Test 25: Process multiple blocks directly on top of the genesis block, then create a block
 ==> FAILED

 Test 26: Construct two branches of approximately equal size, ensuring that blocks are always created on the proper branch
 ==> FAILED

 Test 27: Similar to previous test, but then try to process blocks whose parents are at height < maxHeight - CUT_OFF_AGE
 ==> FAILED


 Total:12/27 tests passed!
 *
 * */