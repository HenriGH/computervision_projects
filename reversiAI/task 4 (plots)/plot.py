import numpy as np
import matplotlib.pyplot as plt

# Simulate n coin flips (1 = heads, -1 = tails)
flips = np.random.choice([1, -1], size=10000)
position = np.cumsum(flips)  # Cumulative sum gives the position over time
    

plt.plot(position, label='Random Experiment 1', color='purple')

# Simulate n coin flips (1 = heads, -1 = tails)
flips = np.random.choice([1, -1], size=10000)
position = np.cumsum(flips)  # Cumulative sum gives the position over time
    

plt.plot(position, label='Random Experiment 2', color='red')

# Simulate n coin flips (1 = heads, -1 = tails)
flips = np.random.choice([1, -1], size=10000)
position = np.cumsum(flips)  # Cumulative sum gives the position over time
    

plt.plot(position, label='Random Experiment 3', color='blue')



plt.axhline(0, color='gray', linestyle='--', linewidth=1)  # Expected Value
plt.title('Random Experiment for Coin Flips (+1 for Heads, -1 for Tails)')
plt.xlabel('Number of Flips')
plt.ylabel('Cumulative Position')
plt.grid(True)
plt.legend()
plt.show()