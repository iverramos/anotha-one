#!/bin/sh
# Build and run. Run from the project root.
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt && java -cp out Main
