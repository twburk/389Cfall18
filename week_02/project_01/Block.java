import com.google.common.hash.Hashing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;

public class Block {

  /* Use the Google Guava module to hash all of your values like so:

     byte[] hash = Hashing.sha256().hashBytes(myByteArray).asBytes();

     The hashBytes method takes a byte array and returns a HashCode,
     so the asBytes() method will return the HashCode as a byte
     array so that you can rehash values easily.

     Side note: As we mentioned in class, SHA-256 is used so that it
     is near impossible to figure out an initial value v if only given
     the hash of v. If you want, you can convince yourself of that by
     trying it here! See if you can guess a byte array just given the
     target Merkle tree hashes present in the public tests! (Spoiler
     Alert: you can't, but it might still be fun to experiment some and
     see how similar values still spit out wildly different hashes :))
  */

  private byte[] prevBlockHash;
  private ArrayList<byte[]> txs;
  private int nonce;
  private byte[] blockHash;
  private byte[] merkleTree;

  public Block(byte[] prevBlockHash, ArrayList<byte[]> txs) {
    this.prevBlockHash = prevBlockHash;
    this.txs = txs;
  }

  public Block() {
    /* Leave empty */
  }

  public byte[] intToByteArray(int nonce) {
    return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(nonce).array();
  }

  /* Build your Merkle tree here. You will need to group your transactions
     together in order (i.e., 0-1, 2-3, 4-5, ... (n - 1)-n) so that you can
     hash the proper root together with the previous block hash and nonce.
     See the project description for more details on tree structure.
   */
  public byte[] buildMerkleTree(ArrayList<byte[]> txs) {
    /* Your code here */
    /*System.arraycopy*/
    if(txs.size() == 1){
      return txs.get(0);
    }else{
      ArrayList<byte[]> newTxs = new ArrayList<>();
      for(int i = 0; i < txs.size(); i= i+2){
        byte[] first = txs.get(i);
        byte[] second = txs.get(i+1);

        byte[] concat = new byte[first.length + second.length]; 

        System.arraycopy(first, 0, concat, 0, first.length);
        System.arraycopy(second, 0, concat, first.length, second.length);

        byte[] hash = Hashing.sha256().hashBytes(concat).asBytes();

        newTxs.add(hash);
      }

      return buildMerkleTree(newTxs);
    }
    
  }

  /* Build your block here. You will need to create a Merkle
     tree out of your transaction list, and hash the root together
     with the previous block id and the correct nonce. Remember, the
     nonce must yield two leading 0s for simplicity (i.e., the
     first two values in your byte array should be 0). When you
     have the correct number of leading 0s, store the byte array in
     this.blockHash

     NOTE: IN ORDER TO REACH THE PROPER TARGET, YOU MUST CONCATENATE YOUR DATA
     IN THIS ORDER: (1) prevBlockHash, (2) merkleTree root, (3) nonce
   */
  public void buildBlock() {
    /* Your code here */
    this.merkleTree = buildMerkleTree(this.txs);
    
    int tempNonce = 0;
    boolean found = false;
    
    do {
    	byte[] byteNonce = intToByteArray(tempNonce);
    	byte [] temp = new byte[this.prevBlockHash.length + this.merkleTree.length + byteNonce.length];
    	
    	System.arraycopy(this.prevBlockHash, 0, temp, 0, this.prevBlockHash.length);
    	System.arraycopy(this.merkleTree, 0, temp, this.prevBlockHash.length, this.merkleTree.length);
    	System.arraycopy(byteNonce, 0, temp, this.prevBlockHash.length+this.merkleTree.length, byteNonce.length);
    	
    	byte[] hash = Hashing.sha256().hashBytes(temp).asBytes();
    	
    	if(hash[0] == 0 && hash[1] == 0) {
    		this.blockHash = hash;
    		this.nonce = tempNonce;
    		found = true;
    	}else {
    		tempNonce++;
    	}
    	
    }while(!found && tempNonce < 100000);
    
  }

  /* This method is intended to validate blocks. Remember that for a block
     to be valid, it has to have the correct number of leading 0s (in
     this case, 2) and if you hash the published prevBlockHash, merkleTree,
     and nonce together, it should equal the published hash of the block (target).
   */
  public boolean isValidBlock(byte[] prevBlockHash, byte[] merkleTree, int nonce, byte[] target) {
    /* Your code here */
	  byte[] byteNonce = intToByteArray(nonce);
	  byte[] temp = new byte[prevBlockHash.length + merkleTree.length + byteNonce.length];
	  
	  System.arraycopy(prevBlockHash, 0, temp, 0, prevBlockHash.length);
  	  System.arraycopy(merkleTree, 0, temp, prevBlockHash.length, merkleTree.length);
  	  System.arraycopy(byteNonce, 0, temp, prevBlockHash.length+merkleTree.length, byteNonce.length);
  	
  	  byte[] hash = Hashing.sha256().hashBytes(temp).asBytes();
  	  
  	  //if(hash[0] == 0 && hash[1] == 0 && hash == target) {
  		//  return true;
  	  //}else {
  		//  return false;
  	  //}
  	  if(Arrays.equals(target, hash)) {
  		  if(hash[0] == 0 && hash[1] == 0) {
  			  return true;
  		  }else {
  			  return false;
  		  }
  	  }else {
  		  return false;
  	  }
  }

  public int getNonce() {
    return this.nonce;
  }

  public byte[] getMerkleTree() {
    return this.merkleTree;
  }

  public byte[] getBlockHash() {
    return this.blockHash;
  }

}
