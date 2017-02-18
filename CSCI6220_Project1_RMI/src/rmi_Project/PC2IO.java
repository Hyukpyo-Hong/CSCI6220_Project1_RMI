package rmi_Project;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PC2IO extends Remote {
	int[][] mult(int[][] arg0, int[][] arg1) throws RemoteException;

	int det(int[][] arg0) throws RemoteException;
}