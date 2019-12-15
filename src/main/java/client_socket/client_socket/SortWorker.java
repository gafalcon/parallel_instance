package client_socket.client_socket;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

public class SortWorker implements Runnable{

	int num_experims;
	String unsortedFilename;
	Consumer<String> onComplete;
	String server_url = App.server_url;
	String FILES_DIR = App.FILES_DIRL;

	public SortWorker(String unsortedFile, Consumer<String> onComplete) {
		this.unsortedFilename = unsortedFile;
		this.onComplete = onComplete;
	}
	
	public static SortWorker startNewSortWorker(String unsortedFile, Consumer<String> onComplete) {
		SortWorker worker = new SortWorker(unsortedFile,  onComplete);
		new Thread(worker).start();
		return worker;
	}
	
	@Override
	public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());	
        System.out.println(String.format("Running sort task on file %s", this.unsortedFilename));
        try {
			Thread.sleep(1000);

			//TODO download files
			this.downloadFile(this.unsortedFilename);
			//TODO Make sort
			
			String sortedFilename = this.sort();
			//TODO upload file
			this.upload(sortedFilename);
			//Send name of merged file
			this.onComplete.accept(sortedFilename);
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
		String fileUrl = String.format("http://%s:7000/mergesortfiles/%s", this.server_url,filename);
        BufferedInputStream inputStream = new BufferedInputStream(new URL(fileUrl).openStream());
       	FileOutputStream fileOS = new FileOutputStream(this.FILES_DIR+this.unsortedFilename); 
   	    byte data[] = new byte[1024];
  	    int byteContent;
 	    while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
 	    	fileOS.write(data, 0, byteContent);
 	    }
 	    fileOS.close();
	}
	
	public String sort() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		File file = new File(this.FILES_DIR+this.unsortedFilename);
		BufferedReader r = null;
		try {
		    r = new BufferedReader(new FileReader(file));
		    String text = null;
		    while ((text = r.readLine()) != null) {
		        list.add(Integer.parseInt(text));
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} 
		finally {
		    try {
		        if (r != null) { r.close();}
		    } catch (IOException e) {e.printStackTrace();}
		}
		Collections.sort(list);
		
		String outfile = "sorted_"+this.unsortedFilename;
		BufferedWriter wr = null;
		try {
			wr = new BufferedWriter(new FileWriter(this.FILES_DIR+outfile));
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		for (int i = 0; i < list.size(); i++) {
		    // Maybe:
			String s = String.valueOf(list.get(i));
			try {
				wr.write(s);
				if(i < list.size()-1) 
					wr.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  }  

		try {
			wr.flush();
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return outfile;
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
		HttpResponse response = client.execute(request);
	}

}
