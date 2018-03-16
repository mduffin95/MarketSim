#!/bin/python3

import glob
import csv
import re
from collections import defaultdict
from functools import reduce

traders = defaultdict(list)
arbitrageurs = defaultdict(list) 
with open("results/results.csv") as csvfile:
    results_reader = csv.reader(csvfile)
    for row in results_reader:
        traders[float(row[0])].append(float(row[1]))
        arbitrageurs[float(row[0])].append(float(row[2]))

x = []
traders_av = []
arb_av = []
for key in sorted(traders):
    x.append(key)
    l = traders[key]
    traders_av.append(reduce((lambda x, y: x+y), l) / len(l))
    l = arbitrageurs[key]
    arb_av.append(reduce((lambda x, y: x+y), l) / len(l))
    
print(x)
print(traders_av)
print(arb_av)

import matplotlib.pyplot as plt

fig = plt.figure(figsize=(11,8))
ax1 = fig.add_subplot(111)

ax1.plot(x, traders_av, label='Traders')
#ax1.plot(x, arb_av, label='Traders')

plt.savefig('results.png')
