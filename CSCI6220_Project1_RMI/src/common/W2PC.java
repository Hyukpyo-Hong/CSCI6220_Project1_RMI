package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface W2PC extends Remote {
	<T> T execute(Task<T> t) throws RemoteException;

	public String getID() throws RemoteException;

}
