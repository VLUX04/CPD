import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Scanner;

public class matrixproduct {

    private static long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() : 0L;
    }

    private static void onMult(int size) {
        double[][] A = new double[size][size];
        double[][] B = new double[size][size];
        double[][] C = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                A[i][j] = 1.0;
                B[i][j] = i + 1;
            }
        }

        long start = System.nanoTime();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double sum = 0;
                for (int k = 0; k < size; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }

        long end = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (end - start) / 1e9);

        System.out.println("Result matrix: ");
        for (int j = 0; j < Math.min(10, size); j++) {
            System.out.print(C[0][j] + " ");
        }
        System.out.println();
    }

    private static void onMultLine(int size) {
        double[][] A = new double[size][size];
        double[][] B = new double[size][size];
        double[][] C = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                A[i][j] = 1.0;
                B[i][j] = i + 1;
            }
        }

        long start = System.nanoTime();
        
        for (int i = 0; i < size; i++) {
            for (int k = 0; k < size; k++) {
                double temp = A[i][k];
                for (int j = 0; j < size; j++) {
                    C[i][j] += temp * B[k][j];
                }
            }
        }

        long end = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (end - start) / 1e9);

        System.out.println("Result matrix: ");
        for (int j = 0; j < Math.min(10, size); j++) {
            System.out.print(C[0][j] + " ");
        }
        System.out.println();
    }

    private static void onMultBlock(int size, int blockSize) {
        double[][] A = new double[size][size];
        double[][] B = new double[size][size];
        double[][] C = new double[size][size];
    
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                A[i][j] = 1.0;
                B[i][j] = i + 1;
                C[i][j] = 0.0; 
            }
        }
        
        int ii, jj, kk, i, j, k;
        long start = System.nanoTime();
    
        for (ii = 0; ii < size; ii += blockSize) {
            for  (kk = 0; kk < size; kk += blockSize)  { 
                for (jj = 0; jj < size; jj += blockSize) {
                    for (i = ii; i < Math.min(ii + blockSize, size); i++) {
                        for (k = kk; k < Math.min(kk + blockSize, size); k++) {
                            for (j = jj; j < Math.min(jj + blockSize, size); j++) {
                                C[i][j] += A[i][k] * B[k][j];
                            }
                        }
                    }
                }
            }
        }
    
        long end = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (end - start) / 1e9);
    
        System.out.println("Result matrix: ");
        for (j = 0; j < Math.min(10, size); j++) {
            System.out.print(C[0][j] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println("\n1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.println("4. Exit");
            System.out.print("Selection?: ");
            option = scanner.nextInt();

            if (option == 1) {
                System.out.print("Dimensions: lins=cols ? ");
                int size = scanner.nextInt();
                onMult(size);
            } else if (option == 2) {
                System.out.print("Dimensions: lins=cols ? ");
                int size = scanner.nextInt();
                onMultLine(size);
            } else if (option == 3) {
                System.out.print("Dimensions: lins=cols ? ");
                int size = scanner.nextInt();
                System.out.print("Block Size? ");
                int blockSize = scanner.nextInt();
                onMultBlock(size, blockSize);
            }
        } while (option != 4);

        scanner.close();
    }
}