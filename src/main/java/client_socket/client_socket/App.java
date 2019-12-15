package client_socket.client_socket;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App implements Runnable
{
	static Thread work_thread;
	static PrintWriter out;
	static String server_url = "localhost";
	static int port = 4000;
    public static void main( String[] args ) throws Exception
    {

        //try (var socket = new Socket("3.19.232.127", 4000)) {
        try (var socket = new Socket(server_url, port)) {
        	System.out.println("Connected to server!");
        	var scanner = new Scanner(System.in);
        	var in = new Scanner(socket.getInputStream());
        	out = new PrintWriter(socket.getOutputStream(), true);
        	while(in.hasNextLine()) {
        		var msg = in.nextLine();
        		//TODO depending on data start a new job
        		if (msg.startsWith("PI")){
        			var msgs = msg.split(",");
        			int num_experims = Integer.parseInt(msgs[1]);
        			PIWorker.startNewPIWorker(num_experims, n -> {
        				out.println("END,"+Integer.toString(n));
        			});
        		}else if (msg.startsWith("MERGE")) {
        			var msgs = msg.split(",");
        			String leftFile = msgs[1];
        			String rightFile = msgs[2];
        			MergeWorker.startNewMergeWorker(leftFile, rightFile, filename -> {
        				out.println(String.format("END,%s", filename));
        			});
        		}else if (msg.startsWith("SORT")) {
        			var msgs = msg.split(",");
        			String unsortedFile = msgs[1];
        			SortWorker.startNewSortWorker(unsortedFile, filename -> {
        				out.println(String.format("END,%s", filename));
        			});
        		}
        		System.out.println(msg);
        		//out.println(scanner.nextLine());
        		
        	}
        	System.out.println("End of connection");
        	scanner.close();
        	in.close();
        	socket.close();
        }
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
        System.out.println("Inside : " + Thread.currentThread().getName());	
        try {
        	//TODO mergesort or pi calculation
			Thread.sleep(10000);
			out.println("End");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
}
