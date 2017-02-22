package pc;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;

import common.Task;
import common.W2PC;

public class Det implements Runnable, Task<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	private int[][] a;
	private int[] mask;
	private int n;
	private W2PC w2pc;
	private Workload workload;

	public void setW2pc(W2PC w2pc) {
		this.w2pc = w2pc;
	}

	public Workload getWorkload() {
		return workload;
	}

	public void setWorkload(Workload workload) {
		this.workload = workload;
	}

	public Det(int[][] a, int[] mask, int n) {
		this.a = a;
		this.mask = mask;
		this.n = n;
	}

	public static long det(int[][] a, int[] mask, int n) {
		long result = 0;

		if (mask == null) {
			mask = new int[n];
			Arrays.fill(mask, 1);
		}

		if (n == 1) {
			int i;
			for (i = 0; i < mask.length; i++) {
				if (mask[i] != 0) {
					break;
				}
			}

			return (long) a[a.length - n][i];
		}
		long sign = +1;
		for (int i = 0; i < mask.length; i++) {
			if (mask[i] == 0) {
				continue;
			}
			mask[i] = 0;
			result += ((long) a[a.length - n][i]) * det(a, mask, n - 1) * sign;
			mask[i] = 1;
			sign = -sign;
		}

		return result;
	}

	@Override
	public void run() {
		try {
			workload.sub_result = w2pc.execute(this);
		} catch (RemoteException e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	@Override
	public Long execute() {
		System.out.println("Compute Determinant..");
		return det(a, mask, n);

	}
}