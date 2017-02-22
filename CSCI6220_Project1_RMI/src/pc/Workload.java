package pc;

import java.io.Serializable;
import java.util.Arrays;

public class Workload implements Serializable {

	int[][] a, b, c;
	long sub_result;

	public Workload[] mulPackage(int[][] aa, int[][] b, int workerSize) {
		Workload[] workload = new Workload[workerSize];
		for (int workerNum = 0; workerNum < workerSize; workerNum++) {
			workload[workerNum] = new Workload();
			int rowSize = ((workerNum + 1) * aa.length / workerSize) - (workerNum * aa.length / workerSize);
			workload[workerNum].a = new int[rowSize][];
			workload[workerNum].b = b;
			workload[workerNum].c = new int[rowSize][aa.length];
			for (int j = 0, i = workerNum * aa.length / workerSize; i < (workerNum + 1) * aa.length
					/ workerSize; i++, j++) {
				workload[workerNum].a[j] = Arrays.copyOfRange(aa[i], 0, aa.length);
			}
		}
		return workload;
	}

	public Workload[] detPackage(int workerSize) {

		// This instances are container to store sub_result
		Workload[] workload = new Workload[workerSize];
		for (int i = 0; i < workerSize; i++) {
			workload[i] = new Workload();
		}

		return workload;
	}
}
