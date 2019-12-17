package client_socket.client_socket;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
public class MergeWorker implements Runnable{

	int num_experims;
	String leftFilename, rightFilename;
	Consumer<String> onComplete;
	String server_url = App.server_url;
	String FILES_DIR = App.FILES_DIRL;

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
			//Thread.sleep(1000);

			this.downloadFile(leftFilename);

			this.downloadFile(rightFilename);
			
			String mergedFilename = this.merge();

			this.upload(mergedFilename);

//			System.out.println("task completed!");
//			System.out.println("type something to finish");
//			//Enter data using BufferReader 
//	        BufferedReader reader =  
//	                   new BufferedReader(new InputStreamReader(System.in)); 
//	         
//	        // Reading data using readLine 
//	        reader.readLine(); 
			this.onComplete.accept(mergedFilename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void downloadFile(String filename) throws MalformedURLException, IOException {
		String fileUrl = String.format("http://%s:7000/mergesortfiles/%s", this.server_url, filename);
        BufferedInputStream inputStream = new BufferedInputStream(new URL(fileUrl).openStream());
       	FileOutputStream fileOS = new FileOutputStream(this.FILES_DIR+filename); 
   	    byte data[] = new byte[1024];
  	    int byteContent;
 	    while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
 	    	fileOS.write(data, 0, byteContent);
 	    }
 	    fileOS.close();
	}
	
	public ArrayList<Integer> readFile(String filename) throws NumberFormatException, IOException {
		ArrayList<Integer> list = new ArrayList<Integer>();
		File file = new File(this.FILES_DIR+filename);
		BufferedReader r = new BufferedReader(new FileReader(file));
		String text = null;
		while ((text = r.readLine()) != null) {
			list.add(Integer.parseInt(text));
		}
		r.close();
		return list;
	}
	
	public String merge() throws NumberFormatException, IOException {
        
        ArrayList<Integer> a = this.readFile(this.leftFilename);
        ArrayList<Integer> b = this.readFile(this.rightFilename);
		
		System.out.println(a);
		System.out.println(b);

		Integer[] array = new Integer[a.size()+b.size()];
		int min_l=0;
		//if(a.size()<b.size()) min_l=a.size(); else min_l=b.size();
	
		
		for(int i=0;!(a.isEmpty()||b.isEmpty());i++)
		{	min_l++;
			if(a.get(0)<b.get(0))
			{
				array[i]=a.get(0);
				a.remove(0);
			}
			else
			{
				array[i]=b.get(0);
				b.remove(0);
			}
		}
		
		if(a.size()==0) 
		{
			for(int i=min_l;i<array.length;i++)
			{	int j=i-min_l;	// System.out.println("Step "+j);
				array[i]=b.get(j);
			}
		}
		else if(b.size()==0)
		{
			for(int i=min_l;i<array.length;i++)
			{	int j=i-min_l;
				array[i]=a.get(j);
			}
		}
		
		//Path rightFilePath = Paths.get(this.leftFilename);
		//Path leftFilePath = Paths.get(this.rightFilename);
		String resfile = "merged_"+ UUID.randomUUID(); 
		
		BufferedWriter wr = null;
		  try {
			wr = new BufferedWriter(new FileWriter(this.FILES_DIR+resfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		  for (int i = 0; i < array.length; i++) {
		    // Maybe:
			  String s = array[i].toString();
		    try {
				wr.write(s);
				if(i < array.length-1) wr.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		  }  
		  try {
			  wr.flush();
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		  
		return resfile; 
	}
	
	public void upload(String filename) throws ClientProtocolException, IOException {
		//TODO upload file to server;
		File file = new File(this.FILES_DIR+filename);
		String posturl = String.format("http://%s:7000/api/resultfile", this.server_url);
		HttpEntity entity = MultipartEntityBuilder.create()
                 .addPart("sortfile", new FileBody(file))
                 .build();

		HttpPost request = new HttpPost(posturl);
		request.setEntity(entity);

		HttpClient client = HttpClientBuilder.create().build();
		client.execute(request);
	}

}