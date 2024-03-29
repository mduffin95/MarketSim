import glob
import pandas as pd
import matplotlib.pyplot as plt
from scipy import stats
import math
from matplotlib2tikz import save as tikz_save

#print(pd.concat([df1,df2],axis=1, join_axes=[df1.index], keys=['foo', 'bar']))

'''
surplus_df_av = surplus_df.groupby(0).mean()
time_sum_df = time_df.groupby(0).sum()
num_trades_sum_df = num_trades_df.groupby(0).sum()
av_time_df = time_sum_df / num_trades_sum_df
'''


def plot_data(df, dof, t, name):
    group = df.groupby(0)
    mean_df = group.mean()
    std_df = group.std(ddof=1)
    err_df = t * (std_df / math.sqrt(dof))
    mean_df.plot(yerr=err_df, capsize=1)
    #plt.show()
    tikz_save(name + ".tex")


#av_time_df.plot()
#plt.show()
if __name__ == "__main__":
    dfs = {}
    result_files = glob.glob("results/tmp/*.csv")
    for r in result_files:
        results = pd.read_csv(r, header=None, index_col=0)
        dfs[r] = results


    all_results = pd.concat(dfs,axis=1)

    surplus_df =    all_results.xs(1, axis=1, level=1)
    time_df =       all_results.xs(2, axis=1, level=1)
    num_trades_df = all_results.xs(3, axis=1, level=1)
    rmsd_df = all_results.xs(5, axis=1, level=1)
    print(num_trades_df)

    dof = surplus_df.groupby(0).count().iloc[0].iloc[0]-1 # Degrees of freedom
    print(dof)
    t = stats.t.ppf(0.95, dof) 
    plot_data(surplus_df, dof, t, "surplus")
    plot_data(time_df / num_trades_df, dof, t, "time")
    plot_data(rmsd_df, dof, t, "rmsd")


