package pc;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;

import common.Task;
import common.W2PC;

public class MultWorkload implements Runnable, Task<int[][]>, Serializable {

	private static final long serialVersionUID = 1L;

	int[][] a, b, c;
	W2PC w2pc;

	public MultWorkload(int[][] aa, int[][] b, int workerNum, W2PC w2pc, int workerSize) {

		this.w2pc = w2pc;
		int rowSize = ((workerNum + 1) * aa.length / workerSize) - (workerNum * aa.length / workerSize);
		this.a = new int[rowSize][aa.length];
		this.b = b;
		this.c = new int[rowSize][aa.length];

		// Make partial matrix a
		for (int j = 0, i = workerNum * aa.length / workerSize; i < (workerNum + 1) * aa.length
				/ workerSize; i++, j++) {
			this.a[j] = Arrays.copyOfRange(aa[i], 0, aa.length);
		}

	}

	@Override
	public void run() {
		try {
			c = w2pc.execute(this);
		} catch (RemoteException e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	@Override
	public int[][] execute() {
		System.out.println("Compute multiplication..");
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				for (int k = 0; k < b.length; k++) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}

}