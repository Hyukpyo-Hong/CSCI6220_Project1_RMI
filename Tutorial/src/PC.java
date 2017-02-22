import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class PC extends UnicastRemoteObject implements PC2W {

    private ArrayList<Remote> Workers;

    protected PC() throws RemoteException {
        super();
        this.Workers = new ArrayList<Remote>();
    }

    public static void main(String args[]) {
        try {

            System.setProperty("java.rmi.server.hostname","PG-01.gswcm.net");
            PC2W PC = new PC();
            Registry R = LocateRegistry.createRegistry(5000);
            R.rebind("PC_SB", PC);
            System.out.println("RMI server is ready");

            for(;;) {

                    int i;
                    boolean flag = false;
                    for(i=0; i<((PC)PC).Workers.size(); i++) {
                        try {
                            String ID = ((W2PC)(((PC)PC).Workers.get(i))).getID();
                            System.out.println(ID);
                        }
                        catch (RemoteException e) {
                            flag = true;
                            break;
                        }
                    }
                    if(flag) {
                        ((PC)PC).Workers.remove(i);
                    }
                    if(((PC)PC).Workers.size() > 0) {
                        System.out.println("--");
                    }
                    //System.out.printf("Number of workers is %d", "", ((PC)PC).Workers.size());
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                }
            }

        }
        catch (RemoteException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }


    }

    @Override
    public boolean reg(Remote server) throws RemoteException {
        return this.Workers.add(server);
    }

    @Override
    public boolean unreg(Remote server) throws RemoteException {
        return this.Workers.remove(server);
    }
}
