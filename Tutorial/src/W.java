import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class W extends UnicastRemoteObject implements W2PC {

    private String ID;

    public W() throws RemoteException {
        super();
        ID = String.valueOf((int)(Math.random() * 1000));
    }

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname","PG-02.gswcm.net");
            W2PC W = new W();
            Registry R = LocateRegistry.getRegistry("pg-01.gswcm.net", 5000);
            PC2W PC = (PC2W)R.lookup("PC_SB");
            PC.reg(W);
            System.out.println("Worker's ID: " + ((W)W).ID);

        }
        catch (RemoteException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (NotBoundException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    @Override
    public String getID() throws RemoteException {
        return this.ID;
    }
}
