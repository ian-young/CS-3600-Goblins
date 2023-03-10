//Binary carver for jpegs
//Proof of concept for 3600 lab assignment
//1-17-19
//Edited 2-9-20
//-Dr. G

import java.io.*;
 
public class og {
    public static void main(String[] args) {
       
       //hard coded test set
        String inputFile = "test1.dd";
        String outputFile = "file1.jpg";
 
        try (
            InputStream inputStream = new FileInputStream(inputFile);
            OutputStream outputStream = new FileOutputStream(outputFile);
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
            	
            	if (byteRead == 255)//Start of header
              	  {
              	  		inputStream.mark(4);//mark the current position
              	  		
              	  		//read in the next 3 bytes for header check
              	  		byte2 = inputStream.read();
              	  		byte3 = inputStream.read();
              	  		byte4 = inputStream.read();
              	  	
              	  	//if next 3 bytes are a match call carving method
              	  	if (byte2 == 216 && byte3==255 && byte4 == 224)
              	  		{carveJpeg(inputStream, outputStream);}
              	  	else
              	  		inputStream.reset(); //if it isn't a match reset to mark
              	  }
            }
 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
   //jpeg carver function assumes you are pointing at the beginning of a jpeg right after
   //the header
   public static void carveJpeg(InputStream inputStream, OutputStream outputStream)
    	{
    				int byteRead;
    				
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