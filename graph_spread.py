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
    #group = df.groupby(0)
    #mean_df = group.mean()
    std_df = group.std(ddof=1)
    err_df = t * (std_df / math.sqrt(dof))
    print(err_df)
    mean_df.plot()
    plt.show()
    #tikz_save(name + ".tex")


#av_time_df.plot()
#plt.show()
if __name__ == "__main__":
    dfs = {}
    std = {}
    result_files = glob.glob("results/tmp/*.csv")
    for r in result_files:
        results = pd.read_csv(r, header=None, index_col=0)
        std[r] = results.groupby(0).std(ddof=1)
        dfs[r] = results.groupby(0).mean()

    dof = 999#spread_df.groupby(0).count().iloc[0].iloc[0]-1 # Degrees of freedom
    t = stats.t.ppf(0.95, dof) 

    all_results = pd.concat(dfs,axis=1)
    all_std = pd.concat(std, axis=1)
    all_err = t * (all_std / math.sqrt(dof))
    print(all_results)
    print(all_std)

    spread_df =    all_results.xs(1, axis=1, level=1)
    volatility_df =       all_results.xs(2, axis=1, level=1)

    spread_err_df = all_err.xs(1, axis=1, level=1)
    volatility_err_df = all_err.xs(2, axis=1, level=1)

    spread_df.plot(yerr=spread_err_df, capsize=1)
    #plt.show()
    tikz_save("spread.tex")
    volatility_df.plot(yerr=volatility_err_df, capsize=1)
    #plt.show()
    tikz_save("volatility.tex")

    #plot_data(spread_df, dof, t, "surplus")
    #plot_data(volatility_df, dof, t, "num_trades")


