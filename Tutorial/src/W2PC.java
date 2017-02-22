import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by simon.baev on 2/9/17.
 */
public interface W2PC extends Remote {
    public String getID() throws RemoteException;
}
