#!/bin/bash

rm -f matrixProduct

# Compile the C++ program
g++ -O2 matrixproduct_test.cpp -o matrixProduct -lpapi -fopenmp

# Function to run tests and capture execution time
run_tests() {
    local start=$1
    local end=$2
    local increment=$3
    local version=$4
    local blockSize=$5
    local baseline_time=0
    local exec_time=0

    for ((size=start; size<=end; size+=increment)); do
        for run in {1..3}; do
            if [ -z "$blockSize" ]; then
                exec_time=$(./matrixProduct $version $size | grep 'Execution time' | awk '{print $3}')
                echo "Running version $version with matrix size $size x $size, run $run: $exec_time" >> results-$version.txt
                if [ $version -eq 1 ]; then
                    baseline_time=$exec_time  # Capture baseline execution time for serial version
                fi
            else
                exec_time=$(./matrixProduct $version $size $blockSize | grep 'Execution time' | awk '{print $3}')
                echo "Running version $version with matrix size $size x $size, block size $blockSize, run $run: $exec_time" >> results-$version.txt
            fi

            # Calculate MFlops only for parallel versions (version 4 and version 5)
            if [[ $version -eq 4 || $version -eq 5 ]]; then
                total_flops=$((2 * size * size * size))  # Assuming 2 * size^3 FLOP for matrix multiplication
                mflops=$(echo "scale=2; $total_flops / $exec_time / 1000000" | bc)
                echo "MFlops: $mflops" >> results-$version.txt
            fi

            # Calculate Speedup and Efficiency only for parallel versions (version 4 and version 5)
            if [[ $version -eq 4 || $version -eq 5 ]]; then
                if [ $baseline_time != 0 ]; then
                    speedup=$(echo "scale=2; $baseline_time / $exec_time" | bc)
                    echo "Speedup: $speedup" >> results-$version.txt
                    # Assuming 1 processor for simplicity; adjust this part based on the number of threads used
                    efficiency=$(echo "scale=2; $speedup / 4" | bc)  # Assume 4 threads, adjust as necessary
                    echo "Efficiency: $efficiency" >> results-$version.txt
                fi
            fi
        done
    done
}

# Run tests for different versions
run_tests 600 3000 400 1    # Serial Version (1)
run_tests 600 3000 400 2    # Line Version (2)
run_tests 4096 10240 2048 2 # Line Version (2) with larger sizes

# Run tests for different block sizes
for blockSize in 128 256 512; do
    run_tests 4096 10240 2048 3 $blockSize # Block Version (3)
done

run_tests 600 3000 400 4    # Parallel Version (Outer Loop) (4)
run_tests 4096 10240 2048 4 # Parallel Version (Outer Loop) (4)
run_tests 600 3000 400 5    # Parallel Version (Inner Loop) (5)
run_tests 4096 10240 2048 5 # Parallel Version (Inner Loop) (5)
