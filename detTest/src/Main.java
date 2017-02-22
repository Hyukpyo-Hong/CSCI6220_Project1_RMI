import java.util.Arrays;

class Worker extends Thread {
	private int[][] A;
	private int[] mask;
	private int N;
	private long result;
	public long getResult() {
		return result;
	}
	public Worker(int[][] A, int[] mask, int N) {
		this.A = A;
		this.mask = mask;
		this.N = N;
		this.start();
	}
	@Override
	public void run() {
		this.result = Main.det(A, mask, N);
	}
}

public class Main {

	public static long det(int[][] A, int[] mask, int N) {
		long result = 0;

		if(mask == null) {
			mask = new int[N];
			Arrays.fill(mask, 1);
		}

		if(N == 1) {
			int i;
			for(i=0; i<mask.length; i++) {
				if(mask[i] != 0) {
					break;
				}
			}

			return A[A.length - N][i];
		}
		int sign = +1;
		for(int i=0; i<mask.length; i++) {
			if(mask[i] == 0) {
				continue;
			}
			mask[i] = 0;
			result += A[A.length - N][i] * det(A, mask, N-1) * sign;
			mask[i] = 1;
			sign = -sign;
		}

		return result;
	};

	public static void main(String[] args) {

		int N = 13;
		int[][] A = new int[N][N];

		//-- Init matrix
		for(int i = 0; i<N; i++)
			for(int j = 0; j<N; j++)
		A[i][j] = (int)(Math.random() * 10);

		//-- Print matrix
		for(int i = 0; i<N; i++) {
			for (int j = 0; j < N; j++)
				System.out.printf("%4d", A[i][j]);
			System.out.println();
		}

		//-- Threads
		int[] mask = new int[N];
		Arrays.fill(mask,1);
		Worker[] W = new Worker[N];
		for(int i = 0; i<N; i++) {
			mask[i] = 0;
			W[i] = new Worker(A, Arrays.copyOf(mask,N), N-1);
			mask[i] = 1;
		}
		//-- Gather results
		long result = 0;
		int sign = +1;
		for(int i = 0; i<N; i++) {
			try {
				W[i].join();
				result += A[0][i] * W[i].getResult() * sign;
				sign = -sign;
			}
			catch (InterruptedException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}

		System.out.println(result);
	}
}
