package pc;

import java.io.Serializable;
import java.rmi.RemoteException;

import common.Task;
import common.W2PC;

public class Mult implements Runnable, Task<int[][]>, Serializable {

	private static final long serialVersionUID = 1L;

	private Workload workload;
	private W2PC w2pc;

	public Mult(W2PC w2pc, Workload workload) {
		this.workload = workload;
		this.w2pc = w2pc;
	}

	@Override
	public void run() {
		try {
			workload.c = w2pc.execute(this);
		} catch (RemoteException e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	@Override
	public int[][] execute() {
		System.out.println("Compute multiplication..");
		for (int i = 0; i < workload.a.length; i++) {
			for (int j = 0; j < workload.b.length; j++) {
				for (int k = 0; k < workload.b.length; k++) {
					workload.c[i][j] += workload.a[i][k] * workload.b[k][j];
				}
			}
		}
		return workload.c;
	}

}