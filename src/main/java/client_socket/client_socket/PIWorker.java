package client_socket.client_socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class PIWorker implements Runnable{

	int num_experims;
	Consumer<Integer> onComplete;

	public PIWorker(int num_experims, Consumer<Integer> onComplete) {
		this.num_experims = num_experims;
		this.onComplete = onComplete;
	}
	
	public static PIWorker startNewPIWorker(int num_experims, Consumer<Integer> onComplete) {
		PIWorker worker = new PIWorker(num_experims, onComplete);
		new Thread(worker).start();
		return worker;
	}
	
	@Override
	public void run() {
        System.out.println("Inside : " + Thread.currentThread().getName());	
        System.out.println(String.format("Running %d calculations", this.num_experims));
        try {
			int res = this.montecarlo(this.num_experims);
//			System.out.println("task completed!");
//			System.out.println("type something to finish");
//			//Enter data using BufferReader 
//	        BufferedReader reader =  
//	                   new BufferedReader(new InputStreamReader(System.in)); 
//	         
//	        // Reading data using readLine 
//	        reader.readLine(); 
			this.onComplete.accept(res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int montecarlo(int n)
	{	int m = 0;
		for(;n>=0;n--) {

			double x = Math.random();
			double y = Math.random();
			if((x*x +y*y)<=1) m++;
		}
		return m ;
		
	}

}
