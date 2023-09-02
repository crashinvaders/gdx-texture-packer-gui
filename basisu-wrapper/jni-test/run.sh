#!/bin/bash
mkdir build
cd build || exit
cmake .. && cmake --build . && cd .. && ./build/basisu-wrapper-test
