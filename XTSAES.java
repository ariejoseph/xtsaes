import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XTSAES {
	public static final int ENCRYPT = 0;
	public static final int DECRYPT = 1;
	
	/*public static void main(String[] args) throws IOException {
		Frame frame = new Frame();
		// Contoh program:
		// siapin variabel yang diperlukan
		String messageFileLocation = "Plain.txt";
		String keyFilename = "Key.txt";
		String Tweaki = "0123456789ABCDEF";
		
		// encrypt
		String cipherFilename = "Cipher.txt";
		//encrypt(messageFileLocation, keyFilename, Tweaki, cipherFilename);
		
		// decrypt
		String decryptedFilename = "Decrypted.txt";
		decrypt(cipherFilename, keyFilename, Tweaki, decryptedFilename);
	}*/
	
	public void encrypt(String messageFileLocation, String keyFilename, String Tweaki, String cipherFilename) throws IOException {
		// Read file and convert to array of byte
		Path messageFilePath = Paths.get(messageFileLocation);
		byte[] message = Files.readAllBytes(messageFilePath); // per byte
		int messageCounter = 0;
		int j = 0;
		boolean needStealing = true;
		int unusedLastBlockSpace = 0;
		if(message.length % 16 == 0) {
			j = message.length/16;
			needStealing = false;
		}
		else {
			j = (message.length/16)+1;
			needStealing = true;
			unusedLastBlockSpace = 16 - (message.length % 16);
		}
		
		// Group the message to 2d array
		// column (first dimension) is per block
		// row (second dimension) is per byte in one block
		//		note: 1 block = 16 byte
		byte[][] blockMessage = new byte[j][16];
		for(int i = 0; i < j; i++) {
			for(int k = 0; k < 16; k++) {
				if(messageCounter < message.length) {
					blockMessage[i][k] = message[messageCounter];
					messageCounter++;
				}
			}
		}
		
		// Read key
		BufferedReader inputKey = new BufferedReader(new FileReader(new File(keyFilename)));
		// Key still in HEX
		String keyStr = inputKey.readLine();
		int keyLength = keyStr.length();
		String keyHex1 = keyStr.substring(0, keyLength/2);
		String keyHex2 = keyStr.substring(keyLength/2, keyLength);
		
		// Convert each key to its char/ascii
		int a = 0;
		String key1 = "";
		while(a < keyHex1.length()) {
			String temp = keyHex1.substring(a,a+2);
			int hex = Integer.parseInt(temp, 16);
			key1 += (char)hex;
			a = a+2;
		}
		byte[] key1arr = key1.getBytes();
		
		a = 0;
		String key2 = "";
		while(a < keyHex2.length()) {
			String temp = keyHex2.substring(a,a+2);
			int hex = Integer.parseInt(temp, 16);
			key2 += (char)hex;
			a = a+2;
		}
		byte[] key2arr = key2.getBytes();
		
		// Tweak
		byte[] TweakArr = Tweaki.getBytes();
		byte[] LittleEndianTweak = new byte[TweakArr.length];
		// Make it little-endian
		for(int idx = 0; idx < TweakArr.length; idx++) {
			LittleEndianTweak[TweakArr.length-(idx+1)] = TweakArr[idx];
		}
		
		// Encrypt
		byte[][] ciphertextArray = xtsAES(XTSAES.ENCRYPT, blockMessage, j, key1arr, key2arr, LittleEndianTweak, needStealing, unusedLastBlockSpace);
		
		printTwo2DByte(blockMessage, ciphertextArray);
		System.out.println("========================================");
		
		// write ciphertext file
		writeByteArrayToFile(ciphertextArray, cipherFilename, message.length);
		
		// Close files
		inputKey.close();
	}
	
	public void decrypt(String messageFileLocation, String keyFilename, String Tweaki, String decryptedFilename) throws IOException {
		// Read file and convert to array of byte
		Path messageFilePath = Paths.get(messageFileLocation);
		byte[] message = Files.readAllBytes(messageFilePath); // per byte
		int messageCounter = 0;
		int j = 0;
		boolean needStealing = true;
		int unusedLastBlockSpace = 0;
		if(message.length % 16 == 0) {
			j = message.length/16;
			needStealing = false;
		}
		else {
			j = (message.length/16)+1;
			needStealing = true;
			unusedLastBlockSpace = 16 - (message.length % 16);
		}
		
		// Group the message to 2d array
		// column (first dimension) is per block
		// row (second dimension) is per byte in one block
		//		note: 1 block = 16 byte
		byte[][] blockMessage = new byte[j][16];
		for(int i = 0; i < j; i++) {
			for(int k = 0; k < 16; k++) {
				if(messageCounter < message.length) {
					blockMessage[i][k] = message[messageCounter];
					messageCounter++;
				}
			}
		}
		
		// Read key
		BufferedReader inputKey = new BufferedReader(new FileReader(new File(keyFilename)));
		// Key still in HEX
		String keyStr = inputKey.readLine();
		int keyLength = keyStr.length();
		String keyHex1 = keyStr.substring(0, keyLength/2);
		String keyHex2 = keyStr.substring(keyLength/2, keyLength);
		
		// Convert each key to its char/ascii
		int a = 0;
		String key1 = "";
		while(a < keyHex1.length()) {
			String temp = keyHex1.substring(a,a+2);
			int hex = Integer.parseInt(temp, 16);
			key1 += (char)hex;
			a = a+2;
		}
		byte[] key1arr = key1.getBytes();
		
		a = 0;
		String key2 = "";
		while(a < keyHex2.length()) {
			String temp = keyHex2.substring(a,a+2);
			int hex = Integer.parseInt(temp, 16);
			key2 += (char)hex;
			a = a+2;
		}
		byte[] key2arr = key2.getBytes();
		
		// Tweak
		byte[] TweakArr = Tweaki.getBytes();
		byte[] LittleEndianTweak = new byte[TweakArr.length];
		// Make it little-endian
		for(int idx = 0; idx < TweakArr.length; idx++) {
			LittleEndianTweak[TweakArr.length-(idx+1)] = TweakArr[idx];
		}
		
		// Decrypt
		byte[][] decryptedArray = xtsAES(XTSAES.DECRYPT, blockMessage, j, key1arr, key2arr, LittleEndianTweak, needStealing, unusedLastBlockSpace);
		
		printTwo2DByte(blockMessage, decryptedArray);
		
		// write decrypted file
		writeByteArrayToFile(decryptedArray, decryptedFilename, message.length);
		
		// Close files
		inputKey.close();
	}
	
	public static byte[][] xtsAES(int activity, byte[][] blockMessage, int j, byte[] key1arr, byte[] key2arr, byte[] LittleEndianTweak, boolean needStealing, int unusedLastBlockSpace){
		// Alpha
		//int alpha = 135;
		
		// Make AES object to encrypt plain text with key 1
		AES objek1 = new AES();
		objek1.setKey(key1arr);
		
		// Make AES object to encrypt tweak with key 2
		AES objek2 = new AES();
		objek2.setKey(key2arr);
		
		// Encryption process start here
		byte[][] PP = new byte[j][16];
		byte[][] CC = new byte[j][16];
		byte[][] ciphertextArray = new byte[j][16];
		
		// 1. Create T
		// Encrypt Key2 + i with AES Encrypt = tweakEncrypted
		byte[] tweakEncrypted = objek2.encrypt(LittleEndianTweak);
		
		// Multiplication alpha^j + tweakEncrypted = T = mul
		// Calculate T FOR ALL BLOCKS
		byte[][] mul = new byte[j+1][16];
		mul[0] = tweakEncrypted;
		for(int i = 0; i < j; i++) {
			for(int k = 0; k < 16; k++) {
				if(k == 0) {
					mul[i+1][k] = 
							(byte) ((2*(mul[i][k] % 128)) ^ 
									(135*(mul[i][15]/128)));
				} else {
					mul[i+1][k] = 
							(byte) ((2*(mul[i][k] % 128)) ^ 
									((mul[i][k-1]/128)));
				}	
			}
		}
		
		// 2. Create PP
		if(j > 2){ // jumlah block harus minimal 2
			// For all block except index j-2 and j-1 (last)
			// Calculate PP for all blocks except block index j-1
			for(int i = 0; i < j-2; i++){ // i represent block number
				for(int p = 0; p < 16; p++) {
					PP[i][p] = (byte) (blockMessage[i][p] ^ mul[i+1][p]);
				}
			}
			
			// 3. Create CC
			// Calculate CC	for all blocks except block index j-1
			for(int i = 0; i < j-2; i++){ // i represent block number
				if(activity == XTSAES.ENCRYPT){
					CC[i] = objek1.encrypt(PP[i]);
				}
				else{
					CC[i] = objek1.decrypt(PP[i]);
				}
			}
			
			// 4. Calculate cipher text 		
			// Calculate cipher text for all blocks except block index j-1
			for(int i = 0; i < j-1; i++){ // i represent block number
				for(int p = 0; p < 16; p++) {
					ciphertextArray[i][p] = (byte) (CC[i][p] ^ mul[i+1][p]);
				}
			}
			
			// ==== Special treatment for block index j-2 & j-1 (last block) ====
			// evaluate block index j-2 
			if(activity == XTSAES.ENCRYPT){
				// PP
				for(int p = 0; p < 16; p++) {
					int i = j-2;
					PP[i][p] = (byte) (blockMessage[i][p] ^ mul[i+1][p]);
				}
				// CC
				if(activity == XTSAES.ENCRYPT){
					int i = j-2;
					CC[i] = objek1.encrypt(PP[i]);
				}
				else {
					int i = j-2;
					CC[i] = objek1.decrypt(PP[i]);
				}
				// ciphertext
				for(int p = 0; p < 16; p++) {
					int i = j-2;
					ciphertextArray[i][p] = (byte) (CC[i][p] ^ mul[i+1][p]);
				}
			}
			else {
				// PP
				for(int p = 0; p < 16; p++) {
					int i = j-2;
					PP[i][p] = (byte) (blockMessage[i][p] ^ mul[i+2][p]); // menggunakan T ke m
				}
				// CC
				if(activity == XTSAES.ENCRYPT){
					int i = j-2;
					CC[i] = objek1.encrypt(PP[i]);
				}
				else {
					int i = j-2;
					CC[i] = objek1.decrypt(PP[i]);
				}
				// ciphertext
				for(int p = 0; p < 16; p++) {
					int i = j-2;
					ciphertextArray[i][p] = (byte) (CC[i][p] ^ mul[i+2][p]); // menggunakan T ke m
				}
			}
			
			// evaluate block index j - 1
			// Append Last Block Plaintext with Ciphertext (size: unusedLastBlockSpace) block number j-2
			int startByteID = 16 - unusedLastBlockSpace;
			int endByteID = 16 - 1;
			byte[] modifiedLastBlock = new byte[16];
			// copy original last block to modifiedLastBlock
			for(int byteID = 0; byteID <= 15; byteID++){
				modifiedLastBlock[byteID] = blockMessage[j-1][byteID];
			}
			
			for(int byteID = startByteID; byteID <= endByteID; byteID++){
				modifiedLastBlock[byteID] = ciphertextArray[j-2][byteID];
			}
			// Calculate PP
			for(int p = 0; p < 16; p++) {
				int i = j - 1;
				if(activity == XTSAES.ENCRYPT){
					PP[i][p] = (byte) (modifiedLastBlock[p] ^ mul[i+1][p]);
				}
				else{
					PP[i][p] = (byte) (modifiedLastBlock[p] ^ mul[i][p]); // menggunakan T ke m-1
				}
				
			}
			// Calculate CC
			if(activity == XTSAES.ENCRYPT){
				CC[j-1] = objek1.encrypt(PP[j-1]);
			}
			else {
				CC[j-1] = objek1.decrypt(PP[j-1]);
			}
			// Calculate ciphertext
			for(int p = 0; p < 16; p++) {
				int i = j-1;
				if(activity == XTSAES.ENCRYPT){
					ciphertextArray[i][p] = (byte) (CC[i][p] ^ mul[i+1][p]);
				}
				else {
					ciphertextArray[i][p] = (byte) (CC[i][p] ^ mul[i][p]); // menggunakan T ke m-1
				}
				
			}
			// Swap j-1 ciphertext with cropped j-2 ciphertext
			byte[] lastCiphertextMaster = new byte[16];
			for(int byteID = 0; byteID <= 15; byteID++){
				lastCiphertextMaster[byteID] = ciphertextArray[j-1][byteID];
			}
			// copy cropped block j-2 to last block
			for(int byteID = 0; byteID <= 15; byteID++){
				
				if(byteID < startByteID){
					ciphertextArray[j-1][byteID] = ciphertextArray[j-2][byteID];
				}
				else {
					ciphertextArray[j-1][byteID] = (byte)0;
				}
			}
			// copy last block (original) to block j - 2 
			for(int byteID = 0; byteID <= 15; byteID++){
				ciphertextArray[j-2][byteID] = lastCiphertextMaster[byteID];
			}
		}
		else{ // jumlah block kurang dari 2
			System.out.println("Jumlah block tidak lebih dari 1");
		}
		
		return ciphertextArray;
	}
	
	public static void writeByteArrayToFile(byte[][] target, String filename, int messageLength) throws IOException{
		
		byte[] flattenedArray = new byte[messageLength];
		int globalID = 0;
		for(int xID = 0; xID < target.length; xID++){
			for(int yID = 0; yID <= 15; yID++){
				if(globalID < messageLength){
					flattenedArray[globalID] = (target[xID][yID]);
					globalID++;
				}
			}
		}
		
		// printing to file
		Path filepath = Paths.get(filename);
		Files.write(filepath, flattenedArray);
	}
	
	public static void print2DByte(byte[][] target){
		for(int p = 0; p < target.length; p++) {
			for(int q = 0; q < target[p].length; q++) {
				System.out.println(target[p][q]);
			}
			System.out.println();
		}
	}
	
	public static void printTwo2DByte(byte[][] target1, byte[][] target2){
		if(target1.length == target2.length){
			for(int p = 0; p < target1.length; p++) {
				for(int q = 0; q < target1[p].length; q++) {
					System.out.print(target1[p][q]);
					System.out.print(" - ");
					System.out.print(target2[p][q]);
					System.out.println();
				}
				System.out.println();
			}
		}
		else {
			System.out.println("Panjang array tidak sama");
		}
	}
	
	public static boolean checkSameElements(byte[][] target1, byte[][] target2){
		boolean returnValue = true;
		if(target1.length == target2.length){
			for(int p = 0; p < target1.length; p++) {
				for(int q = 0; q < target1[p].length; q++) {
					if(target1[p][q] != target2[p][q]){
						returnValue = false;
						System.out.print("Catch: p=" + p + " - q=" + q + " : ");
						System.out.println(target1[p][q] + " - " + target2[p][q]);
					}
				}
			}
		}
		else{
			returnValue = false;
		}
		return returnValue;
	}
	
	public static void printThree2DByte(byte[][] target1, byte[][] target2, byte[][] target3){
		if(target1.length == target2.length){
			for(int p = 0; p < target1.length; p++) {
				for(int q = 0; q < target1[p].length; q++) {
					System.out.print(target1[p][q]);
					System.out.print(" - ");
					System.out.print(target2[p][q]);
					System.out.print(" - ");
					System.out.print(target3[p][q]);
					System.out.println();
				}
				System.out.println();
			}
		}
		else {
			System.out.println("Panjang array tidak sama");
		}
	}
}
