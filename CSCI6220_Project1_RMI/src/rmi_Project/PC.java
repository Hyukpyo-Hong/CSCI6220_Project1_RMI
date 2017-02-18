package rmi_Project;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

public class PC extends UnicastRemoteObject implements PC2IO {
	private static final long serialVersionUID = 1L;

	protected PC() throws RemoteException {
	}

	public static void main(String[] args) {
		String ip = getIP().substring(1); // remove first character '/'
		String registryName = "PC2IO";
		short port = 2080;

		try {
			System.setProperty("java.rmi.server.hostname", ip);
			PC2IO pc = new PC();
			Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind(registryName, pc);
			System.out.println("PC(" + ip + ") is bounded.");

		} catch (RemoteException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

	}

	@Override
	public int[][] mult(int[][] a, int[][] b) throws RemoteException {
		int size = a.length;
		int[][] c = new int[size][size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}

	@Override
	public int det(int[][] matrix) throws RemoteException {
		int det = 0;
		int size = matrix.length;

		switch (size) {
		case 1: // 1 x 1
			return matrix[0][0];
		case 2: // 2 x 2
			return (matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]);
		default: // more than 3 x 3
		}

		for (int k = 0; k < size; k++) {

			// compute Minor of Matrix[0][j]
			int[][] minor = new int[size - 1][size - 1];
			int r, c; // row and column of minor matrix
			r = 0;
			for (int i = 0; i < size; i++) {
				if (i != 0) {
					c = 0;
					for (int j = 0; j < size; j++) {
						if (j != k) {
							minor[r][c++] = matrix[i][j];
						}
					}
					r++;
				}
			}

			// Compute Determinant recursively with fixed first row.
			det += matrix[0][k] * (int) Math.pow(-1, k) * det(minor);
		}
		return det;
	}

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