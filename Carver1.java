/* Painstakingly made by:
 * Ian Young 2/23/2023
 *  The purpose of this program is to carve jpg files from a data dump file (.dd)
 * This will NOT read EXIF files. This divides the input file into chunks which
 * are to be read by threads. These threads will call the method "carveJpeg"
 * when a matching jpg header is found. From there, the file is carved and
 * outputted to a .jpg file. ENJOY!
*/
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

public class Carver1 {
    public static void main(String[] args) {
        //hard coded test set
        String inputFile = "GoblinsV2.dd"; // File to carve
        File huntingGround = new File(inputFile);
        
        long packSize = huntingGround.length(); // Length of the file
        int numThreads = Runtime.getRuntime().availableProcessors(); // Threads available to play
        long[] chunkSize = new long[numThreads]; // Chunk size for each thread to look at

        for(int i = 0; i < numThreads; i++)
        // Create starting positions for each thread
            chunkSize[i] = packSize * i / numThreads;

            Scout[] seek = new Scout[numThreads];
            for(int i = 0;i < numThreads;i++)
                seek[i] = new Scout(chunkSize[i], inputFile, packSize, i, numThreads);

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(seek[i]);
            threads[i].start();
            System.out.println(seek[i].getID() + " has joined the hunt!");
        }

        // Jpegs start with ff d8 ff e0
		// Some jpegs could end in e#
		// If a jpeg has extended file header information (EXIF) it will have two ff d8
		// ff e#'s
		// The decimal equivalent is 255 216 255 224
    }

    //jpeg carver function assumes you are pointing at the beginning of a jpeg right after
    //the header
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

    static class Scout implements Runnable {
        // Initialize variables to be used in this class
        private long begin; // Starting position for the thread
        private InputStream inputStream;
        private long position; // Track the position -> avoids reading twice
        private long packSize;
        private int id;
        private int numThread;
        
        public static int headCount = 0; // Tracks how many goblins have been found

        public Scout(long begin, String inputFile, long packSize,int id, int numThread) {
            InputStream inputStream;
            try {
                this.begin = begin;
                inputStream = new FileInputStream(inputFile);
                this.inputStream = new BufferedInputStream(inputStream);
                this.packSize = packSize;
                this.id = id;
                this.numThread = numThread;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.err.println("Non-existant file passed through.");
        }
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            int byteRead;
            int byte2;
            int byte3;
            int byte4;

            try {
                inputStream.skip(begin); // Starts reading at thread's starting pos

                // While loop will stop at the end of the stream or at the end of the chunk given
                while((byteRead = inputStream.read()) != -1 && position < packSize / numThread) {
                    position++; // Advance!!!!
                    if (byteRead == 255)// Start of header
					{
						inputStream.mark(4);// mark the current position

						// Read in the next 3 bytes for header check
						byte2 = inputStream.read();
						byte3 = inputStream.read();
						byte4 = inputStream.read();

						// If jpg header is found, pass to carver
                        // This avoids overlap carving which is the issue I was facing
						if (byte2 == 216 && byte3 == 255 && byte4 == 224) {
							OutputStream outputStream = new FileOutputStream("Thread" + id + ".Gob" + headCount + ".jpg"); // Unique file output creation
							headCount++; //update file count
							System.out.println(id+" spotted it's " + headCount + " goblin!");
							position =+ carveJpeg(inputStream, outputStream); //carve and update position
							System.out.println(id+" identified it's " + headCount + " goblin!");
						} else
							inputStream.reset(); // If it isn't a match, try again
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Uhhhh....");
			}
        }

        public int getID() {
            return id;
        }
    }
}