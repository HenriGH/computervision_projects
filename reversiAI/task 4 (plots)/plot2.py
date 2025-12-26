import numpy as np
import matplotlib.pyplot as plt

# Parameters
num_steps = 10000     
num_walks = 500       


walks = []

for x in range(num_walks):
    steps = np.random.choice([-1, 1], size=num_steps)  
    walk = np.cumsum(steps)  
    walks.append(np.abs(walk))  

# Convert list to numpy array for easy averaging
walks = np.array(walks)
average_abs = np.mean(walks, axis=0)

# Plotting
plt.figure(figsize=(10, 5))
plt.plot(average_abs, label='Average distance from mean over time')
plt.xlabel('Step')
plt.ylabel('Average Absolute Value')
plt.title(f'Average Absolute Value of {num_walks} Experiments')
plt.grid(True)
plt.legend()
plt.show()
