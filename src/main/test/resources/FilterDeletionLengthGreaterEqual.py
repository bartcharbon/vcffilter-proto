#!/usr/bin/python
import sys

dict = {}
split = sys.argv[1].split(",")
for x in split:
    y = x.split(":")
    dict[y[0]] = y[1]

altLength = len(dict["ALT"])
refLength = len(dict["REF"])
expected = int(dict["VALUE_ARG"])
difference = refLength - altLength
print(difference >= expected)
sys.exit(0)
