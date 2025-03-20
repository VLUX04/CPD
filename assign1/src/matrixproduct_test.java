public class matrixproduct_test {

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

        int i, j, k;
        long start = System.nanoTime();
        
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                double sum = 0;
                for (k = 0; k < size; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }

        long end = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (end - start) / 1e9);
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

        int i, j, k;
        long start = System.nanoTime();
        
        for (i = 0; i < size; i++) {
            for (k = 0; k < size; k++) {
                double temp = A[i][k];
                for (j = 0; j < size; j++) {
                    C[i][j] += temp * B[k][j];
                }
            }
        }

        long end = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (end - start) / 1e9);
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
            for (jj = 0; jj < size; jj += blockSize) {
                for (kk = 0; kk < size; kk += blockSize) {

                    int iMax = Math.min(ii + blockSize, size);
                    int jMax = Math.min(jj + blockSize, size);
                    int kMax = Math.min(kk + blockSize, size);

                    for (i = ii; i < iMax; i++) {
                        for (k = kk; k < kMax; k++) {
                            double temp = A[i][k]; 
                            for (j = jj; j < jMax; j++) {
                                C[i][j] += temp * B[k][j];
                            }
                        }
                    }
                }
            }
        }

        long end = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (end - start) / 1e9);
        System.out.println();
    }

    public static void main(String[] args) {
        // Ensure there are enough arguments passed
        if (args.length < 2) {
            System.out.println("Usage: java matrixproduct <version> <size> [blockSize]");
            return;
        }

        int version = Integer.parseInt(args[0]);  // First argument: version
        int size = Integer.parseInt(args[1]);     // Second argument: matrix size
        int blockSize = (args.length > 2) ? Integer.parseInt(args[2]) : 0;  // Optional block size

        // Call the corresponding multiplication function based on the version
        if (version == 1) {
            onMult(size);
        } else if (version == 2) {
            onMultLine(size);
        } else if (version == 3) {
            if (blockSize == 0) {
                System.out.println("Block size must be provided for version 3.");
                return;
            }
            onMultBlock(size, blockSize);
        } else {
            System.out.println("Invalid version.");
        }
    }
}