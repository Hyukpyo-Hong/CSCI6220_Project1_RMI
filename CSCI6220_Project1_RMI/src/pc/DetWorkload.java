package pc;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;

import common.Task;
import common.W2PC;

public class DetWorkload implements Runnable, Task<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	private int[][] a;
	private int[] mask;
	private int n;
	private W2PC w2pc;
	long sub_result;

	public DetWorkload(int[][] a, int[] mask, int n, W2PC w2pc) {
		this.a = a;
		this.mask = mask;
		this.n = n;
		this.w2pc = w2pc;
	}

	@Override
	public void run() {
		try {
			sub_result = w2pc.execute(this);
		} catch (RemoteException e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	@Override
	public Long execute() {
		System.out.println("Compute Determinant..");
		return det(a, mask, n);

	}

	// Below computing determinant code refer to class lecture.
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

}