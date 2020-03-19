#!/usr/bin/python
import sys

dict = {}
split = sys.argv[1].split(",")
for x in split:
    y = x.split(":")
    dict[y[0]] = y[1]

print(dict["#CHROM"] == "X")
sys.exit(0)
