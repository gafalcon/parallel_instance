package client_socket.client_socket;

import java.lang.management.ManagementFactory;
import java.util.function.Consumer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.sun.management.OperatingSystemMXBean;

public class CPUMeasure implements Runnable {
	MBeanServer mbs;
	ObjectName name;
	OperatingSystemMXBean osBean;
	AttributeList list;
	int MAX_READS = 3;
	double cpu_performance;
	Consumer<Double> onMeasure;

	public CPUMeasure(Consumer<Double> onMeasure) {
		this.onMeasure = onMeasure;
	}

	public static CPUMeasure startCPUMeasure(Consumer<Double> onMeasure) {
		CPUMeasure cpuMeasure = new CPUMeasure(onMeasure);
		new Thread(cpuMeasure).start();
		return cpuMeasure;
	}

	public double readCpuPercentage() throws InstanceNotFoundException, ReflectionException {
		list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		if (list.isEmpty()) 
			return Double.NaN;
		Attribute att = (Attribute)list.get(0);
		Double value  = (Double)att.getValue();
		if (value == -1.0)      
			return Double.NaN;
		return ((int)(value * 1000) / 10.0);
	}

	@Override
	public void run() {
	    mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			osBean = ManagementFactory.getPlatformMXBean(
					OperatingSystemMXBean.class);

			double cpu_pctg = Double.NaN, load_avg;
			double cpu_avg = 0;
			double num_reads = 0;
			while(true) {
				try {
					while(num_reads < MAX_READS) {
						cpu_pctg = this.readCpuPercentage();
						System.out.println(String.format("cpu: %f", cpu_pctg));
						if (cpu_pctg != Double.NaN) {
							cpu_avg += cpu_pctg;
							num_reads++;
						}
						Thread.sleep(3000);
					}
					this.cpu_performance = cpu_avg / MAX_READS;
					//System.out.println(String.format("AVG CPU: %f", cpu_performance));
					this.onMeasure.accept(this.cpu_performance);
					cpu_avg = 0;
					num_reads = 0;
				} catch (InstanceNotFoundException | ReflectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//load_avg = (osBean.getSystemLoadAverage());
				//System.out.println(String.format("cpu: %f, load avg: %f", cpu_pctg, load_avg));
			}
		} catch (MalformedObjectNameException | NullPointerException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
