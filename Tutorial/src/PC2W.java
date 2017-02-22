import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PC2W extends Remote {
    public boolean reg(Remote server) throws RemoteException;
    public boolean unreg(Remote server) throws RemoteException;
}
