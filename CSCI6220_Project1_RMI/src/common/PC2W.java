package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PC2W extends Remote {

	public boolean unreg(W2PC w) throws RemoteException;

	boolean reg(W2PC w) throws RemoteException;

	public void checkConnection() throws RemoteException;

}