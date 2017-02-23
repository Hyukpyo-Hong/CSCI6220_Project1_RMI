package pc;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import common.PC2IO;
import common.PC2W;
import common.W2PC;

public class PC extends UnicastRemoteObject implements PC2IO, PC2W {
	private static final long serialVersionUID = 1L;
	private static ArrayList<W2PC> Workers;
	static PC2W pc2w;

	protected PC() throws RemoteException {
		super();
		Workers = new ArrayList<W2PC>();
	}

	// Below managing connection code refer to the class lecture.
	public static void main(String[] args) {

		String ip = getIP().substring(1); // remove first character '/'
		short port_pc2io = 2080;
		short port_pc2w = 2081;

		try {
			// Bind pw2io and pc2w registry
			System.setProperty("java.rmi.server.hostname", ip);
			PC2IO pc2io = new PC();
			pc2w = new PC();
			Registry registry_pc2io = LocateRegistry.createRegistry(port_pc2io);
			Registry registry_pc2w = LocateRegistry.createRegistry(port_pc2w);
			registry_pc2io.rebind("PC2IO", pc2io);
			System.out.println("PC(" + ip + ") is bounded for IO module connection.");
			registry_pc2w.rebind("PC2W", pc2w);
			System.out.println("PC(" + ip + ") is bounded for Worker module connection.");

			// Monitor connection status
			for (;;) {
				int i;
				boolean flag = false;
				System.out.printf("\r%60s\r", "");
				System.out.print("Connected IP : ");
				for (i = 0; i < Workers.size(); i++) {
					try {
						String ID = Workers.get(i).getID();
						System.out.print(ID + " ");
					} catch (RemoteException e) {
						flag = true;
						break;
					}
				}
				if (flag) {
					Workers.remove(i);
				}
				try {
					Thread.sleep(1000);

				} catch (InterruptedException e) {
				}
			}
		} catch (RemoteException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	@Override
	public boolean reg(W2PC w) throws RemoteException {
		System.out.println(w.getID());
		return Workers.add(w);
	}

	@Override
	public boolean unreg(W2PC w) throws RemoteException {
		return Workers.remove(w);
	}

	@Override
	public void checkConnection() throws RemoteException {
		// If a worker failed to invoke this method, it will be terminated.
	}

	@Override
	public int[][] mult(int[][] a, int[][] b) throws RemoteException {

		// Assign the multiplication workload to an each worker thread.
		int workerSize = Workers.size();
		MultWorkload[] mulWorkload = new MultWorkload[workerSize];
		Thread thread[] = new Thread[workerSize];

		for (int i = 0; i < workerSize; i++) {
			mulWorkload[i] = new MultWorkload(a, b, i, Workers.get(i), workerSize);
			thread[i] = new Thread(mulWorkload[i]);
			thread[i].start();
			System.out.println("\nMultiplication Thread " + i + " start..");
		}

		// Combine the result of each worker thread into matrix C
		int[][] c = new int[a.length][a.length];
		for (int w = 0; w < workerSize; w++) {
			try {
				thread[w].join();
				for (int w_i = 0, i = w * c.length / workerSize; i < (w + 1) * c.length / workerSize; w_i++, i++) {
					for (int j = 0; j < c.length; j++) {
						c[i][j] = mulWorkload[w].c[w_i][j];
					}
				}
				System.out.println("\nMultiplication  Thread " + w + " finished..");
			} catch (InterruptedException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
		return c;
	}

	@Override
	public long det(int[][] a) throws RemoteException {

		long result = 0;
		int workerSize = Workers.size();
		int n = a.length;
		int[] mask = new int[n];
		Arrays.fill(mask, 1);
		int sign = +1;

		DetWorkload[] detWorkload = new DetWorkload[workerSize];
		Thread[] thread = new Thread[workerSize];

		// Two queues to manage masking, assigning and combining order.
		Queue<Integer> working_index = new LinkedList<Integer>();
		Queue<Integer> idle_index = new LinkedList<Integer>();
		for (int i = 0; i < workerSize; i++) {
			idle_index.add(i);
		}
		int mask_index = 0;
		int combine_index = 0;
		boolean check_Det_Finish = false;

		// Check idle and working queues while determinant flag is not set.
		while (!check_Det_Finish) {
			// Assign new workload to idle workers
			while (!idle_index.isEmpty()) {
				int index = idle_index.poll();

				mask[mask_index] = 0;
				detWorkload[index] = new DetWorkload(a, Arrays.copyOf(mask, n), n - 1, Workers.get(index));
				thread[index] = new Thread(detWorkload[index]);
				thread[index].start();
				System.out.println("\nDeterminant Thread(Worker) " + index + " start..");
				mask[mask_index] = 1;

				working_index.add(index);
				if (mask_index == n - 1)
					break;
				mask_index++;
			}

			// Combine sub-result from job finished workers.
			if (!working_index.isEmpty()) {
				int index = working_index.poll();
				try {
					thread[index].join();
					result += a[0][combine_index] * detWorkload[index].sub_result * sign;
					sign = -sign;
					idle_index.add(index);
					System.out.println("\nDeterminant Thread(Worker) " + index + " finished..");
				} catch (InterruptedException e) {
					System.err.println("Error: " + e.getMessage());
				}
				if (combine_index == n - 1)
					check_Det_Finish = true;
				combine_index++;
			}
		}
		return result;
	}

	// Below 'IP finder' code refer to
	// 'https://wiki.gswcm.net/doku.php?id=howto:code:ip_list'
	public static String getIP() {
		try {
			Enumeration<NetworkInterface> NICs = NetworkInterface.getNetworkInterfaces();
			while (NICs.hasMoreElements() == true) {
				NetworkInterface NIC = NICs.nextElement();
				Enumeration<InetAddress> IPs = NIC.getInetAddresses();
				while (IPs.hasMoreElements() == true) {
					InetAddress IP = IPs.nextElement();
					if (IP instanceof java.net.Inet4Address) {
						return IP.toString();
					}
				}
			}
		} catch (SocketException e4) {
			System.err.println("Error: getNetworkInterfaces() failed: " + e4);
		}
		System.out.println("IP is not found.");
		return null;
	}

}
