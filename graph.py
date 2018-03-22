#!/bin/python3

import glob
import csv
import re
from collections import defaultdict
from functools import reduce

import matplotlib.pyplot as plt

results ={}
result_files = glob.glob("results/tmp/*.csv")
averaged = {}
for r in result_files:
    surplus = defaultdict(list)

    with open(r) as csvfile:
        results_reader = csv.reader(csvfile)
        for row in results_reader:
            surplus[float(row[0])].append(float(row[1]))
    results[r] = surplus 

    x_vals = []
    av = []
    for key in sorted(surplus):
        x_vals.append(key)
        l = surplus[key]
        av.append(reduce((lambda x, y: x+y), l) / len(l))
    averaged[r] = av 

print(x_vals)
print(averaged)

def plot(x, ys, labels):
    fig = plt.figure(figsize=(11,8))
    ax1 = fig.add_subplot(111)
    ax1.set_xlabel('latency')
    ax1.set_ylabel('surplus')
    for y, l in zip(ys, labels):
        ax1.plot(x_vals, y, label=l)

    plt.savefig('results.png')


ys = []
labels = []
for key, val in averaged.items():
    labels.append(key)
    ys.append(val)

plot(x_vals, ys, labels)

