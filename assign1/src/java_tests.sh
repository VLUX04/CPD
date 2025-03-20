#!/bin/bash

# Function to run tests and capture execution time
run_tests() {
    local start=$1
    local end=$2
    local increment=$3
    local version=$4
    local blockSize=$5
    local baseline_time=0
    local exec_time=0

    # Check if the Java program exists
    if [ ! -f matrixproduct_test.class ]; then
        echo "Compiling matrixproduct_test.java..."
        javac matrixproduct_test.java
    fi

    for ((size=start; size<=end; size+=increment)); do
        for run in {1..3}; do
            if [ -z "$blockSize" ]; then
                # Run the Java program and capture the execution time
                exec_time=$(java matrixproduct_test $version $size | grep 'Time:' | awk '{print $2}')
                echo "Running version $version with matrix size $size x $size, run $run: $exec_time" >> results-java-$version.txt

            else
                # Run the Java program with block size and capture the execution time
                exec_time=$(java matrixproduct_test $version $size $blockSize | grep 'Time:' | awk '{print $2}')
                echo "Running version $version with matrix size $size x $size, block size $blockSize, run $run: $exec_time" >> results-java-$version.txt
            fi
        done
    done
}

# 1. From 600x600 to 3000x3000 matrix sizes with increments of 400 for the onMult() version
run_tests 600 3000 400 1

# 2. From 600x600 to 3000x3000 matrix sizes with increments of 400 for the onMultLine() version
run_tests 600 3000 400 2

# 3. For different block sizes for the onMultBlock() version
for blockSize in 128 256 512; do
    run_tests 4096 10240 2048 3 $blockSize
done

