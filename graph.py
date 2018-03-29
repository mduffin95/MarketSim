#!/bin/python3

import glob
import csv
import re
from collections import defaultdict
from functools import reduce
import numpy as np
from scipy import stats
import math

import matplotlib.pyplot as plt
from matplotlib2tikz import save as tikz_save

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

    dof = len(surplus[0])-1
    latencies = []
    av = []
    err = []
    t = stats.t.ppf(0.95, dof) 
    for key in sorted(surplus):
        latencies.append(key)
        l = surplus[key]
        a = np.mean(l, axis=0)
        e = t * (np.std(l, axis=0, ddof=1) / math.sqrt(dof))
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
        print(labels[i])
        ax1.errorbar(xs[i], ys[i], yerr=err[i], label=labels[i], markersize=8, capsize=10)
        

    ax1.legend()
    plt.savefig('results.png')
    tikz_save('results.tex')

plot(x_vals, averaged, error, result_files)

