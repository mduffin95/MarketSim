#!/bin/python3

import glob
import csv
import re
from collections import defaultdict
from functools import reduce
import numpy as np

import matplotlib.pyplot as plt

result_files = glob.glob("results/tmp/*.csv")
averaged = [] 
error = []
x_vals = []
for r in result_files:
    surplus = defaultdict(list)

    with open(r) as csvfile:
        results_reader = csv.reader(csvfile)
        for row in results_reader:
            surplus[float(row[0])].append(float(row[1]))

    latencies = []
    av = []
    err = []
    for key in sorted(surplus):
        latencies.append(key)
        l = surplus[key]
        a = np.mean(l, axis=0)
        e = 1.96 * np.std(l, axis=0, ddof=1) 
        av.append(a)
        err.append(e)
    averaged.append(av) 
    error.append(err) 
    x_vals.append(latencies)

print(len(x_vals))
print(len(averaged))
print(len(error))

def plot(xs, ys, err, labels):
    fig = plt.figure(figsize=(11,8))
    ax1 = fig.add_subplot(111)
    ax1.set_xlabel('latency')
    ax1.set_ylabel('surplus')
    for i in range(len(xs)):
        ax1.errorbar(xs[i], ys[i], yerr=err[i], label=labels[i])
        

    plt.savefig('results.png')

plot(x_vals, averaged, error, result_files)

