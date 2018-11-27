import java.util.*;


public class TxHandler {

    private UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        /* your code here */
    	pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} as inputs are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        /* your code here */
    	ArrayList<Transaction.Input> inputs = tx.getInputs();
    	ArrayList<Transaction.Output> outputs = tx.getOutputs();
    	double sum = 0;
    	double poolSum = 0;
    	
    	/*Checking for Bullet 1*/
    	for(int i = 0; i < inputs.size(); i++) {
    		Transaction.Input input = tx.getInput(i);
    		UTXO newUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    		if(!pool.contains(newUTXO)) {
    			return false;
    		}
    		
    	}
  
    	/*Checking for Bullet 2*/
    	for(int i = 0; i < inputs.size(); i++) {
    		Transaction.Input input = tx.getInput(i);
    		UTXO newUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    		Transaction.Output output = pool.getTxOutput(newUTXO);
    		byte[] rawData = tx.getRawDataToSign(i);
    		
    		if(!Crypto.verifySignature(output.address, rawData, input.signature)) {
    			return false;
    		}
    		
    	}
    	
    	/*Checking for Bullet 3*/
    	Map multiple = new HashMap();
    	for(int i = 0; i < inputs.size(); i++) {
    		Transaction.Input input = tx.getInput(i);
    		UTXO newUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    		if(multiple.containsKey(newUTXO)) {
    			return false;
    		}else {
    			multiple.put(newUTXO, 1);
    		}
    	}
    	
    	/*Checking for Bullet 4*/
    	for(int i = 0; i < outputs.size(); i++) {
    		Transaction.Output output = tx.getOutput(i);
    		if(output.value < 0) {
    			return false;
    		}else {
    			sum += output.value;
    		}
    	}
    	
    	/*Checking for Bullet 5*/
    	for(int i = 0; i < inputs.size(); i++) {
    		//poolSum = 0;
    		Transaction.Input input = tx.getInput(i);
    		UTXO newUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    		Transaction.Output out = pool.getTxOutput(newUTXO);
    		poolSum += out.value;
    		
    	}
    	if(poolSum < sum) {
    		return false;
    	}  	
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        /* your code here */
    	ArrayList<Transaction> valid = new ArrayList<Transaction>();
    	Transaction[] validArray;
    	
    	for(int i = 0; i < possibleTxs.length; i++) {
    		if(isValidTx(possibleTxs[i])) {
    			valid.add(possibleTxs[i]);
    			ArrayList<Transaction.Input> inputs = possibleTxs[i].getInputs();
    			ArrayList<Transaction.Output> outputs = possibleTxs[i].getOutputs();
    			for(int j = 0; j < inputs.size(); j++) {
    				Transaction.Input input = possibleTxs[i].getInput(j);
    				UTXO newUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    				pool.removeUTXO(newUTXO);
    			}
    			
    			for(int j = 0; j < outputs.size(); j++) {
    				byte[] newHash = possibleTxs[i].getHash();
    				UTXO newUTXO = new UTXO(newHash, j);
    				Transaction.Output output = possibleTxs[i].getOutput(j);
    				pool.addUTXO(newUTXO, output);
    			}
    		}
    	}
    	
    	validArray = new Transaction[valid.size()];
    	
    	for(int i = 0; i < valid.size(); i++) {
    		validArray[i] = valid.get(i);
    	}
    	
    	
    	return validArray;
    }

}
