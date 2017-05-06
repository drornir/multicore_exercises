import pandas as pd

df = pd.read_csv("./out1.txt",sep="\t", engine="python", names=["time","counter"], skiprows=[0])

df.index = df.time.apply(lambda x:x.split(":")[0])
df.index.name = None

df = df.iloc[:-1]

df.time = df.time.apply(lambda x: int(x.replace(",","").split(":")[-1].lstrip()))

df.counter = df.counter.apply(lambda x: int(x.split(":")[-1].lstrip()))

df.to_csv("./plot_ready_data.csv")
