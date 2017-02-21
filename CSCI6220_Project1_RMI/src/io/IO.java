package rmi_Project;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class IO {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		int m = Integer.parseInt(args[0]);
		String host = "168.18.104.56"; // PG-01.gswcm.net
		short port = 2080;
		String registryName = "PC2IO";

		if (parameterCheck(m)) {
			int[][] a = generateMatrix(m);
			int[][] b = generateMatrix(m);
			int[][] c = new int[m][m];

			generateFile(a, m, "A.txt");
			generateFile(b, m, "B.txt");
			System.out.println("Matrix A and B are generated");

			try {
				Registry r = LocateRegistry.getRegistry(host, port);
				PC2IO pc2io = (PC2IO) r.lookup(registryName);

				System.out.println("IO is connected to " + host + "\n");
				System.out.println(">>>Computing matrix C...");
				c = pc2io.mult(a, b);
				generateFile(c, m, "C.txt");
				System.out.println("Matrix C is generated\n");

				System.out.println(">>>Computing determinant...");
				int det = pc2io.det(a);
				System.out.println("Determinant of Matrix A :" + det);

			} catch (RemoteException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			} catch (NotBoundException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		} else {
			System.out.println("Parameters are invaild.");
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("\nExecution Time : " + elapsedTime + "ms.");
	}

	static int[][] generateMatrix(int m) {
		byte min = -100;
		byte max = 100;
		int[][] array = new int[m][m];
		Random random = new Random();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				array[i][j] = random.nextInt(max + 1 - min) + min;
			}
		}
		return array;
	}

	static void generateFile(int[][] a, int m, String fileName) {
		PrintStream output = null;

		try {
			output = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));

			for (int i = 0; i < m; i++) {
				for (int j = 0; j < m; j++) {
					output.print(a[i][j]);
					if (j != m - 1) {
						output.print(" ");
					}
				}

				if (i != m - 1) {
					output.println();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File writing error occured.");
			e.printStackTrace();
		} finally {
			if (output != null) {
				output.close();
			}

		}

	}

	static boolean parameterCheck(int m) {
		int flag = 0;
		if (m <= 0) {
			flag++;
			System.out.println("Size of matrix must be bigger than 0.");
		}

		return flag <= 0;
	}
}