import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Carver1 {
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        //hard coded test set
        String inputFile = "test1.dd";
        String outputFile = "file1.jpg";

        try (
            InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream outputStream = new FileOutputStream(outputFile);
        ) {
            int fileSize = inputStream.available();

            int numThreads = Runtime.getRuntime().availableProcessors();
            int chunkSize = fileSize / numThreads;

            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                int start = i * chunkSize;
                int end = (i == numThreads - 1) ? fileSize : start + chunkSize;

                threads[i] = new Thread(() -> {
                    try {
                        carveJpgs(inputStream, outputStream, start, end);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void carveJpgs(InputStream inputStream, OutputStream outputStream, int start, int end) throws IOException {
        inputStream.skip(start);

        //input stream returns bytes in the form of integer values
        int byteRead;
        int byte2;
        int byte3;
        int byte4;

        //Jpegs start with ff d8 ff e0
        //Some jpegs could end in e#
        //If a jpeg has extended file header information (EXIF) it will have two ff d8 ff e#'s
        //The decimal equivalent is 255 216 255 224
        while (((BufferedInputStream)inputStream).read() < end && (byteRead = ((BufferedInputStream)inputStream).read()) != -1) {
            if (byteRead == 255)//Start of header
            {
                inputStream.mark(4);//mark the current position

                //read in the next 3 bytes for header check
                byte2 = ((BufferedInputStream)inputStream).read();
                byte3 = ((BufferedInputStream)inputStream).read();
                byte4 = ((BufferedInputStream)inputStream).read();

                //if next 3 bytes are a match call carving method
                if (byte2 == 216 && byte3==255 && byte4 == 224)
                {
                    carveJpeg(inputStream, outputStream);
                }
                else
                {
                    ((BufferedInputStream)inputStream).reset(); //if it isn't a match reset to mark
                }
            }
        }
    }

    //jpeg carver function assumes you are pointing at the beginning of a jpeg right after
    //the header
    public static void carveJpeg(InputStream inputStream, OutputStream outputStream) throws IOException {
        int byteRead;

        //write the header
        outputStream.write(255);
        outputStream.write(216);
        outputStream.write(255);
        outputStream.write(224);

        //write loop until you find the footer ff d9 -> 255 217
        while ((byteRead = ((BufferedInputStream)inputStream).read()) != -1)
        {
            outputStream.write(byteRead);

            //if you find an ff look for a d9
            if (byteRead == 255)
            {
                byteRead = ((BufferedInputStream)inputStream).read();
                outputStream.write(byteRead);

                if(byteRead == 217)
                {
                    outputStream.write(byteRead);
                    break; //this is the end
				}
			}
		}//end while
	}
}