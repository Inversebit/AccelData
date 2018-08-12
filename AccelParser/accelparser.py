#############################################
# Copyright (c) 2018 Inversebit
#
# This code is free under the MIT License.
# Full license text: https://opensource.org/licenses/MIT
#
# Accelparser. This code parses certain CSV files containing
# accelerometer data and creates a few graphs to study recorded
# events.
#
# Samples available under the 'ba' folder. Uncompress 'samples.zip'
# and replace 'test.csv' with the selected file on line 31.
#
#############################################

import pandas as pd
import matplotlib.pyplot as plt

GRAPH_WINDOW = 300
ROLLING_AVG_WINDOW = 5

GRAVITY_FORCE = 9.8

MAX_BUMP_GRAPHS = 10


def main():
    print(">>> Start")

    #Load CSV
    df = pd.read_csv('ba/test.csv')

    #Remove gravity force from Z axis
    df['accZ'] = df['accZ'] - GRAVITY_FORCE

    #Apply rolling AVG to smothen wave
    df['accXAvg'] = df['accX'].rolling(window=ROLLING_AVG_WINDOW).mean()
    df['accYAvg'] = df['accY'].rolling(window=ROLLING_AVG_WINDOW).mean()
    df['accZAvg'] = df['accZ'].rolling(window=ROLLING_AVG_WINDOW).mean()

    #Get indices of registered bumps
    res = df['b'][df['b'] == 1].index

    for bump in res[0:MAX_BUMP_GRAPHS]:
        #Apply window to take N values before and after bump
        start = bump - GRAPH_WINDOW
        fin = bump + GRAPH_WINDOW

        #Prepare for 2 graphs, raw wave around bump and the smoothened one
        fig, axes = plt.subplots(nrows=1, ncols=2, figsize=(15,5))
        df.iloc[start:fin].plot(ax=axes[0], x='ts', y=['accX', 'accY', 'accZ'])
        df.iloc[start:fin].plot(ax=axes[1], x='ts', y=['accXAvg', 'accYAvg', 'accZAvg'])

    #Apply legend and avoid that the graphs close as soon as the program ends
    plt.legend()
    plt.show(block=True)

    print(">>> Finish")


if __name__ == "__main__":
    main()
