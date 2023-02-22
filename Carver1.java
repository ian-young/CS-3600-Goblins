import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Carver1 {
    public static void main(String[] args) {
        //hard coded test set
        String inputFile = "test1.dd";

        File hunt = new File(inputFile);

        // House keeping
        if (hunt.exists()) {
            long fileSize = inputFile.length();

            int numThreads = Runtime.getRuntime().availableProcessors();
            long[] chunkSize = new long[numThreads]; // each thread will have its own chunk

            // Starting positions
            for(int i = 0; i < numThreads; i++) {
                chunkSize[i] = fileSize * i / numThreads;
            }

            // Hunter creation
            Finder[] finder = new Finder[numThreads];
            for(int i = 0; i < numThreads; i++) {
                finder[i] = new Finder(chunkSize[i], inputFile, fileSize, i, numThreads);
            }

            // Thread creation
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(finder[i]);
                threads[i].start();
                System.out.println(finder[i].threadID + " has joined the hunt.");
            }

           
        }
    }


    public static int carveJpeg(InputStream inputStream, OutputStream outputStream) {
        int byteRead;
        int count = 0;
        try {
            // write the header
            outputStream.write(255);
            outputStream.write(216);
            outputStream.write(255);
            outputStream.write(224);

            // write loop until you find the footer ff d9 -> 255 217
            while ((byteRead = inputStream.read()) != -1) {
                outputStream.write(byteRead);
                count++;

                // if you find an ff look for a d9
                if (byteRead == 255) {
                    byteRead = inputStream.read();
                    count++;
                    outputStream.write(byteRead);

                    if (byteRead == 217) {
                        outputStream.write(byteRead);
                        break; // this is the end
                    }
                }
            } // end while
        } // end try
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    static class Finder implements Runnable {

        public long start; // Starting position
        public InputStream inputStream; // Each thread needs its own stream!!
        public long position; // Position pointer
        public long fileSize; // Size of the file
        public int threadID; // Unique thread id
        private int threadNum; // Number of threads being used
        
        public static int fileCount = 0; // Track how many files have been found

    public Finder(long start, String inputFile, long fileSize, int threadID, int threadNum){
        try {
            InputStream inputStream = new FileInputStream(inputFile);
            this.inputStream = new BufferedInputStream(inputStream);
            this.fileSize = fileSize;
            this.threadID = threadID;
            this.start = start;
            this.threadNum = threadNum;
        } catch (FileNotFoundException e) {
            System.err.println("The file passed through does not exist!!!");
        }
    }

    @Override
    public void run() {
        //input stream returns bytes in the form of integer values
        int byteRead;
        int byte2; 
        int byte3; 
        int byte4;

         //Jpegs start with ff d8 ff e0
        //Some jpegs could end in e#
        //If a jpeg has extended file header information (EXIF) it will have two ff d8 ff e#'s
         //The decimal equivalent is 255 216 255 224
         
        try {
            while ((byteRead = inputStream.read()) != -1 && position < fileSize / threadNum) {
                position++; // Update
                if (byteRead == 255) // Start of header
                    {
                            inputStream.mark(4);//mark the current position
                            
                            //read in the next 3 bytes for header check
                            byte2 = inputStream.read();
                            byte3 = inputStream.read();
                            byte4 = inputStream.read();
                        
                        //if next 3 bytes are a match call carving method
                        if (byte2 == 216 && byte3==255 && byte4 == 224) {
                            // File found, open stream. One thread may find more than one file.
                            OutputStream outputStream = new FileOutputStream("file" + threadID + "-" + fileCount + ".jpg");
                            fileCount++; // update count
                            System.out.println(threadID + " has been hit");
                            position=+carveJpeg(inputStream, outputStream); // Update
                            System.out.println(threadID + " has been netrualized");
                        }
                        else
                            inputStream.reset(); //if it isn't a match reset to mark
                    }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    }
}
