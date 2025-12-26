import pandas as pd
import matplotlib.pyplot as plt
import os
from pathlib import Path


# Python script that plots every numeric column of a csv file and saves the individual plots as pngs in the measurements folder
# This script is not really for generating plots that will show up in the report but more so to get a quick visualisation of whatever data was collected
# input path has to be adjusted, labeling too (if we want it to be accurate)

# Get input and output path
script_path = Path(__file__).resolve()

project_root = script_path.parents[1]

csv_file = project_root / "measurements/measurements_2025-05-16_22-08-58.csv"  
output_dir = project_root / "measurements"               


df = pd.read_csv(csv_file)


# Select numeric columns only
numeric_df = df.select_dtypes(include=["number"])

# Plot each numeric column
for column in numeric_df.columns:
    plt.figure()
    numeric_df[column].plot(title="Move Time", marker='o')
    plt.xlabel("Move")
    plt.ylabel("Nanoseconds")
    plt.grid(True)
    plt.tight_layout()
    plot_path = os.path.join(output_dir, f"{column}_plot.png")
    plt.savefig(plot_path)
    plt.close()
    
