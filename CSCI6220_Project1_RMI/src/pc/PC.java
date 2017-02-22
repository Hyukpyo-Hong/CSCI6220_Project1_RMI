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

	public static void main(String[] args) {

		String ip = getIP().substring(1); // remove first character '/'
		short port_pc2io = 2080;
		short port_pc2w = 2081;

		try {
			System.setProperty("java.rmi.server.hostname", ip);
			PC2IO pc2io = new PC();
			pc2w = new PC();
			Registry registry_pc2io = LocateRegistry.createRegistry(port_pc2io);
			Registry registry_pc2w = LocateRegistry.createRegistry(port_pc2w);
			registry_pc2io.rebind("PC2IO", pc2io);
			System.out.println("PC(" + ip + ") is bounded for IO module connection.");
			registry_pc2w.rebind("PC2W", pc2w);
			System.out.println("PC(" + ip + ") is bounded for Worker module connection.");

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
		int[][] c = new int[a.length][a.length];
		int workerSize = Workers.size();
		Workload[] workload = new Workload().mulPackage(a, b, workerSize);
		Runnable th;
		Thread thread[] = new Thread[workerSize];

		for (int i = 0; i < workerSize; i++) {
			th = new Mult(Workers.get(i), workload[i]);
			thread[i] = new Thread(th);
			thread[i].start();
			System.out.println("Multiplication Thread " + i + " start..");
		}

		for (int w = 0; w < workerSize; w++) {
			try {
				thread[w].join();
				System.out.println("Multiplication  Thread " + w + " finished..");
				for (int w_i = 0, i = w * c.length / workerSize; i < (w + 1) * c.length / workerSize; w_i++, i++) {
					for (int j = 0; j < c.length; j++) {
						c[i][j] = workload[w].c[w_i][j];
					}
				}
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
		int assign_index = 0;
		int combine_index = 0;
		boolean check_Det_Finish = false;

		Det temp;
		Thread[] thread = new Thread[workerSize];
		Workload[] workload = new Workload().detPackage(workerSize);
		Queue<Integer> working_index = new LinkedList<Integer>();
		Queue<Integer> idle_index = new LinkedList<Integer>();
		for (int i = 0; i < workerSize; i++) {
			idle_index.add(i);
		}
		while (!check_Det_Finish) {
			while (!idle_index.isEmpty()) {

				int index = idle_index.poll();
				mask[assign_index] = 0;
				temp = new Det(a, Arrays.copyOf(mask, n), n - 1);
				temp.setW2pc(Workers.get(index));
				temp.setWorkload(workload[index]);
				thread[index] = new Thread(temp);
				thread[index].start();
				System.out.println("Determinant Thread(Worker) " + index + " start..");
				mask[assign_index] = 1;
				working_index.add(index);
				if (assign_index == n - 1)
					break;
				assign_index++;
			}

			if (!working_index.isEmpty()) {
				int index = working_index.poll();
				try {
					thread[index].join();
					result += a[0][combine_index] * workload[index].sub_result * sign;
					sign = -sign;
					idle_index.add(index);
					System.out.println("Determinant Thread(Worker) " + index + " finished..");
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

	// Below 'IP finder' code comes from
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
