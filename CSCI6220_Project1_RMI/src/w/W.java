package w;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

import common.PC2W;
import common.Task;
import common.W2PC;

public class W extends UnicastRemoteObject implements W2PC {
	private static final long serialVersionUID = 1L;
	private String ID;

	public W() throws RemoteException {
		super();
		ID = getIP().substring(1);
	}

	public static void main(String[] args) {
		String host = "168.18.104.56"; // PG-01.gswcm.net
		short port_pc2w = 2081;
		String ip = getIP().substring(1); // remove first character '/'

		try {
			System.setProperty("java.rmi.server.hostname", ip);
			W2PC w = new W();
			Registry r = LocateRegistry.getRegistry(host, port_pc2w);
			PC2W pc = (PC2W) r.lookup("PC2W");
			pc.reg(w);
			System.out.println("Worker's ID: " + ((W) w).ID);
			while (true) {
				try {
					pc.checkConnection();
					Thread.sleep(2000);
				} catch (RemoteException e) {
					System.out.println("Disconnected by PC side");
					System.err.println(e.getMessage());
					System.exit(1);
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}

			}
		} catch (RemoteException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (NotBoundException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	@Override
	public <T> T execute(Task<T> t) throws RemoteException {
		return t.execute();
	}

	@Override
	public String getID() throws RemoteException {
		return this.ID;
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
