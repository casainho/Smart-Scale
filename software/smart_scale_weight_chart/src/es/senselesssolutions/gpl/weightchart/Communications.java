package es.senselesssolutions.gpl.weightchart;

import java.util.Vector;

public class Communications {
	
	Vector<Integer> mMessageVector = new Vector<Integer>();
	Vector<Integer> mReceivedMessageVector = new Vector<Integer>();
	private final int startByte = 0xff;
	public int mStateReceiveMesssage = 0;
	public static int mCounterReceiveMesssage = 0;
	public static int mBytesCounterReceiveMesssage = 0;
	public static int mLenReceiveMessage = 0;
	CRC16 CRCReceiveMessage = new CRC16();
	public int mCRCReceiveMessage = 0;
	
	// Add CRC to message
	// Input: String with full message to send
	// Output: byte[] array with message ready to send
	//
	// At the end, Vector must be:
	// [0xff] [0xff] [0xff] [0xff] [len] [0xbe] [0xef] [..] [..] [crc1] [crc0]
	// CRC is for [len] [0xbe] [0xef] [..] [..]
	byte[] readyMessageToSend (String message) {
		mMessageVector.clear();
		
		// store string message bytes to vector
    	int messageSize = message.length();
    	byte[] messageBytes = new byte[messageSize];
    	messageBytes = message.getBytes();
    	  	
    	for (int i = 0; i < messageSize; i++) {
    		mMessageVector.add(messageBytes[i] & 0xff);
    	}
    	
    	mMessageVector.insertElementAt(0xbe, 0);
    	mMessageVector.insertElementAt(0xef, 0);
    	messageSize += 2;
    
    	// adding length value to vector
		messageSize++;
		mMessageVector.insertElementAt(messageSize + 2, 0);
		
		// adding CRC
		CRC16 cRC = new CRC16();
		int cRCValue = cRC.calc(mMessageVector, messageSize);
		mMessageVector.add(cRCValue >> 8);
		mMessageVector.add(cRCValue & 0xff);
		messageSize += 2;
    	
    	// adding 4*startByte to begin of the vector
		mMessageVector.insertElementAt(startByte, 0);
		mMessageVector.insertElementAt(startByte, 0);
		mMessageVector.insertElementAt(startByte, 0);
		mMessageVector.insertElementAt(startByte, 0);
		
		byte[] byteArrayMessage = new byte[mMessageVector.size()];
    	for (int i = 0; i < (messageSize + 4); i++) {
    		byteArrayMessage[i] = (byte) ((mMessageVector.get(i)) & 0xff);
    	}
    	
    	return byteArrayMessage;
	}
	
	// Receive  
	// Verify CRC
	//
	//
	//
	String[] receiveMessage (byte[] messageBytes, int messageBytesIndex, int messageSize) {
		String[] returnMessage = new String[2];
		
    	for ( ; messageBytesIndex < messageSize; messageBytesIndex++) {
    		switch (mStateReceiveMesssage) {
    		case 0: // Find first 4 0xff bytes
    			if ((messageBytes[messageBytesIndex] & 0xff) == 0xff) {
    				mCounterReceiveMesssage++;
    				if (mCounterReceiveMesssage == 4) {
    					mCounterReceiveMesssage = 0;
    					mStateReceiveMesssage = 1;
    				}
    			} else {
    				mCounterReceiveMesssage = 0;
    			}
    			break;
    			
    		case 1: // add length to Vector
    			mLenReceiveMessage = messageBytes[messageBytesIndex];
    			mReceivedMessageVector.add(mLenReceiveMessage);
    			mBytesCounterReceiveMesssage++;
    			mStateReceiveMesssage = 2;
    			break;
    			
    		case 2: // get all data byte to Vector
    			
    			// If we didn't got yet the length bytes... 
    			if (mBytesCounterReceiveMesssage < mLenReceiveMessage) {
    				mReceivedMessageVector.add((int) messageBytes[messageBytesIndex]);
    				mBytesCounterReceiveMesssage++;

    				// If we got already all length bytes...
    				if(mBytesCounterReceiveMesssage == mLenReceiveMessage) {
        				// we got a packet, now verify CRC
    					int t = mReceivedMessageVector.lastElement();
    					if (t < 0) {
    						mCRCReceiveMessage = 256 + t;
    					} else {
    						mCRCReceiveMessage = t;
    					}
        				mReceivedMessageVector.removeElementAt((mReceivedMessageVector.size()-1));
        				
        				t = mReceivedMessageVector.lastElement();
    					if (t < 0) {
    						mCRCReceiveMessage += (256 + t) * 256;
    					} else {
    						mCRCReceiveMessage += t * 256;
    					}
    					mReceivedMessageVector.removeElementAt((mReceivedMessageVector.size()-1));		
        				
        				if (mCRCReceiveMessage == (CRCReceiveMessage.calc(mReceivedMessageVector, (mLenReceiveMessage-2)))){// (CRCReceiveMessage.calc(mReceivedMessageVector, (mLenReceiveMessage-2)))) {
        					// CRC is ok
        					mReceivedMessageVector.removeElementAt(0); // remove length byte
        					    			    	
        					byte[] byteArrayMessage = new byte[(mReceivedMessageVector.size() - 2)];
        			    	for (int j = 2; j < (mLenReceiveMessage - 3); j++) {
        			    		byteArrayMessage[j - 2] = (byte) ((mReceivedMessageVector.get(j)) & 0xff);
        			    	}
        			    	
        			    	String s = new String(byteArrayMessage);
        			    	returnMessage[0] = s;
        			    	returnMessage[1] = Integer.toString(messageBytesIndex);
        			    	
        			    	mBytesCounterReceiveMesssage = 0;
        			    	mStateReceiveMesssage = 0;
        			    	mReceivedMessageVector.clear();

        			    	return returnMessage;
        					
        				} else {
        					// Bad CRC, so we got wrong data/wrong packet
        	    			mBytesCounterReceiveMesssage = 0;
        	    			mStateReceiveMesssage = 0;
        	    			mReceivedMessageVector.clear();
        				}
        			}
    			} else {
    				// Wrong message length?
	    			mBytesCounterReceiveMesssage = 0;
	    			mStateReceiveMesssage = 0;        
	    			mReceivedMessageVector.clear();
    			}
    			break;
    			
    		}	
    	}
    	
    	returnMessage[0] = null;
    	returnMessage[1] = Integer.toString(messageBytesIndex);
		return returnMessage;
	}
}

class CRC16 {
	
	int[] crc_tab16 = new int[256];
	int crc_tab16_init = 0;
	int crc = 0;
	final int P_16 = 0xA001;
	
	public CRC16 () {
		CRCTableInit();
	}
	
    int calc(Vector<Integer> messageVector, int messageSize) {
    	CRCInit();
    	
    	for (int i = 0; i < messageSize; i++) {
    		UpdateCRC(messageVector.get(i));
    	}
    	
    	return crc;
    }
    
    void CRCTableInit () {
	  int i, j, crc, c;

	  for (i=0; i<256; i++) {
	      crc = 0;
	      c = i;

	      for (j = 0; j < 8; j++) {
        	  if (((crc ^ c) & 0x0001) > 0) {
	        	  	crc = (crc >> 1) ^ P_16;
        	  } else {
        		  crc = crc >> 1;
        	  }

	          c = c >> 1;
	      }

	      crc_tab16[i] = crc;
	  }

	  crc_tab16_init = 1;
    }
    
    void UpdateCRC(int value) {
      int tmp, short_c;

      short_c = 0x00ff & value;

      tmp = crc ^ short_c;
      crc = (crc >> 8) ^ crc_tab16[tmp & 0xff];
      }
    
    void CRCInit() {
    	crc = 0;
    }
}
