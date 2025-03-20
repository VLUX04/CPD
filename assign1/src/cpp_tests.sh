#!/bin/bash

# Increase stack size and heap size limits
ulimit -s unlimited
ulimit -v unlimited

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
    local exec_time=0
    local l1_dcm=0
    local l2_dcm=0

    for ((size=start; size<=end; size+=increment)); do
        for run in {1..3}; do
            if [ -z "$blockSize" ]; then
                result=$(./matrixProduct $version $size)
                exec_time=$(echo "$result" | grep 'Execution time' | awk '{print $3}')
                l1_dcm=$(echo "$result" | grep 'L1 DCM' | awk '{print $3}')
                l2_dcm=$(echo "$result" | grep 'L2 DCM' | awk '{print $3}')
                echo "Running version $version with matrix size $size x $size, run $run: $exec_time, L1 DCM: $l1_dcm, L2 DCM: $l2_dcm" >> results-cpp-$version.txt
            else
                result=$(./matrixProduct $version $size $blockSize)
                exec_time=$(echo "$result" | grep 'Execution time' | awk '{print $3}')
                l1_dcm=$(echo "$result" | grep 'L1 DCM' | awk '{print $3}')
                l2_dcm=$(echo "$result" | grep 'L2 DCM' | awk '{print $3}')
                echo "Running version $version with matrix size $size x $size, block size $blockSize, run $run: $exec_time, L1 DCM: $l1_dcm, L2 DCM: $l2_dcm" >> results-cpp-$version.txt
            fi

            # Calculate MFlops only for parallel versions (version 4 and version 5)
            if [[ $version -eq 4 || $version -eq 5 ]]; then
                total_flops=$((2 * size * size * size))  # Assuming 2 * size^3 FLOP for matrix multiplication
                mflops=$(echo "scale=2; $total_flops / $exec_time / 1000000" | bc)
                echo "MFlops: $mflops" >> results-cpp-$version.txt
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
