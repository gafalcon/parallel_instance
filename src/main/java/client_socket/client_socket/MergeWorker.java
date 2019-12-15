package client_socket.client_socket;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
public class MergeWorker implements Runnable{

	int num_experims;
	String leftFilename, rightFilename;
	Consumer<String> onComplete;
	String server_url = App.server_url;

	public MergeWorker(String leftFile, String rightFile, Consumer<String> onComplete) {
		this.leftFilename = leftFile;
		this.rightFilename = rightFile;
		this.onComplete = onComplete;
	}
	
	public static MergeWorker startNewMergeWorker(String leftFile, String rightFile, Consumer<String> onComplete) {
		MergeWorker worker = new MergeWorker(leftFile, rightFile, onComplete);
		new Thread(worker).start();
		return worker;
	}
	
	@Override
	public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());	
        System.out.println(String.format("Running merge task"));
        try {
			Thread.sleep(10000);

			//TODO download files
			this.downloadFile(leftFilename);
			this.downloadFile(rightFilename);
			//TODO Make merge
			
			String mergedFilename = this.merge();
			//TODO upload file
			this.upload(mergedFilename);
			//Send name of merged file
			this.onComplete.accept(mergedFilename);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void downloadFile(String filename) throws MalformedURLException, IOException {
		String fileUrl = String.format("http://%s:7000/unsorted/%s", this.server_url, filename);
        BufferedInputStream inputStream = new BufferedInputStream(new URL(fileUrl).openStream());
       	FileOutputStream fileOS = new FileOutputStream(filename); 
   	    byte data[] = new byte[1024];
  	    int byteContent;
 	    while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
 	    	fileOS.write(data, 0, byteContent);
 	    }
 	    fileOS.close();
	}
	
	public String merge() {
		// TODO mergeFiles and return name of mergedFile	
		Path rightFilePath = Paths.get(this.leftFilename);
		Path leftFilePath = Paths.get(this.rightFilename);
		return leftFilePath.getFileName().toString() + rightFilePath.getFileName().toString();
	}
	
	public void upload(String filename) {
		//TODO upload file to server;
	}

}