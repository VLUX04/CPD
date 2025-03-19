#!/bin/bash

# Function to run tests
run_tests() {
    local start=$1
    local end=$2
    local increment=$3
    local version=$4
    local blockSize=$5

    # Check if the Java program exists
    if [ ! -f matrixproduct.class ]; then
        echo "Compiling matrixproduct.java..."
        javac matrixproduct.java
    fi

    for ((size=start; size<=end; size+=increment)); do
        for run in {1..3}; do
            if [ -z "$blockSize" ]; then
                echo "Running version $version with matrix size $size x $size, run $run"
                java matrixproduct $version $size
            else
                echo "Running version $version with matrix size $size x $size, block size $blockSize, run $run"
                java matrixproduct $version $size $blockSize
            fi
        done
    done
}

# 1. From 600x600 to 3000x3000 matrix sizes with increments of 400 for the onMult() version
run_tests 600 3000 400 1

# 2. From 600x600 to 3000x3000 matrix sizes with increments of 400 for the onMultLine() version
run_tests 600 3000 400 2

# 3. From 4096x4096 to 10240x10240 with intervals of 2048 for the onMultLine() version
run_tests 4096 10240 2048 2

# 4. For different block sizes for the onMultBlock() version
for blockSize in 128 256 512; do
    run_tests 4096 10240 2048 3 $blockSize
done
