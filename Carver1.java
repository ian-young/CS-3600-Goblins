//Multi-threaded Binary carver for jpegs
//Edited 2-13-23
//-Ian Young

import java.io.*;

class Carver implements Runnable {
	// Data is sent here to be carved out

	int byteRead;
	private InputStream inputStream;
	private OutputStream outputStream;

	Carver(InputStream inputStream, OutputStream outputStream) {
		//jpeg carver function assumes you are pointing at the beginning of a jpeg right after
   		//the header
    		this.inputStream = inputStream;
			this.outputStream = outputStream;				
	}

	public void run() {
		try {
			//write the header
			outputStream.write(255);
			outputStream.write(216);
			outputStream.write(255);
			outputStream.write(224);
			
			//write loop until you find the footer ff d9 -> 255 217
			while ((byteRead = inputStream.read()) != -1) 
			{
				outputStream.write(byteRead);
				
				//if you find an ff look for a d9
				if (byteRead == 255)
					{
						byteRead = inputStream.read();
						outputStream.write(byteRead);
						
							if(byteRead == 217)
								{
									outputStream.write(byteRead);
									break; //this is the end
								}
					}
			}//end while
		}//end try
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
 
public class Carver1 {
    public static void main(String[] args) {
       
       //hard coded test set
        String inputFile = "test1.dd";
        String outputFile;
		Thread[] threads = new Thread[10];
 
        try (
            InputStream inputStream = new FileInputStream(inputFile);
        ) {
 
 			//input stream returns bytes in the form of integer values
            int byteRead;
            int byte2; 
            int byte3; 
            int byte4;
 
 			//Jpegs start with ff d8 ff e0
			//Some jpegs could end in e#
			//If a jpeg has extended file header information (EXIF) it will have two ff d8 ff e#'s
 			//The decimal equivalent is 255 216 255 224
 			
            while ((byteRead = inputStream.read()) != -1) {
				
            	int tCount = 0; // Counts how many threads are in use.
				outputFile = "Out" + tCount + ".jpg"; // dynamic name based on thread count for the outputted file
				OutputStream outputStream = new FileOutputStream(outputFile); // Set the output stream based off the new file name

            	if (byteRead == 255)//Start of header
              	  {
              	  		inputStream.mark(4);//mark the current position
              	  		
              	  		//read in the next 3 bytes for header check
              	  		byte2 = inputStream.read();
              	  		byte3 = inputStream.read();
              	  		byte4 = inputStream.read();
              	  	
              	  	//if next 3 bytes are a match call carving method
              	  	if (byte2 == 216 && byte3==255 && byte4 == 224) {
					// Create a new thread to carve and add it to the array.
              	  		// {carveJpeg(inputStream, outputStream);}
						if (tCount < threads.length)
							expandArray(threads);
						threads[tCount] = new Thread(new Carver(inputStream, outputStream));
					}
              	  	else
              	  		inputStream.reset(); //if it isn't a match reset to mark
              	  }
            }
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

	public static Thread[] expandArray(Thread[] threads){
		// Doubles the size of an array

		Thread[] doubledArray = new Thread[threads.length * 2];
		for (int i = 0;i < threads.length; i++) {
			doubledArray[i] = threads[i];
		}
		return doubledArray;
	}

}