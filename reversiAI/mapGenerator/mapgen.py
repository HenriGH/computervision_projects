from PIL import Image
import numpy as np
inputpath = "inputs/input3.jpg"
while True:
    try:
        inputpath = input("Enter the input files name (.jpg): ")
        break  # Exit loop if input is valid
    except ValueError:
        print("That's not a valid input. Please try again.")


# Load the image and convert to grayscale
img = Image.open(inputpath).convert("L")  # "L" mode is for grayscale

# Convert to NumPy array (2D)
img_array = np.array(img)

while True:
    try:
        playernum = int(input("Enter the number of players: "))
        if((playernum < 2) | (playernum > 8)):
            raise ValueError
        break  # Exit loop if input is valid
    except ValueError:
        print("That's not a valid integer. Please try again.")



steps = np.zeros(playernum+6, dtype=int)
steps[0] = 0
steps[playernum + 1] = 180
steps[playernum + 2] = 200
steps[playernum + 3] = 220
steps[playernum + 4] = 240
steps[playernum + 5] = 255


#fill array 
current = 0
for x in range (playernum):
    current += round(160/playernum)
    steps[x+1] = current



#check wether input has the right shape (max 50x50)
if(len(img_array) > 50 | len(img_array[1]) > 50):
    print("Invalid input dimensions!")

#process intesity values by rounding to the next step
def process(num):
    for x in range (len(steps)):
        if(num == steps[x]):
            return num
    for x in range(len(steps)):
        if(num<steps[x]):
            if(num-steps[x-1] < steps[x] - num):
                return steps[x-1]
            else:
                return steps[x]

def int_to_char(num):
    if(num == 0):
        return '-'
    elif(num == 180):
        return 'c'
    elif(num == 200):
        return 'i'
    elif(num == 220):
        return 'b'
    elif(num == 240):
        return 'x'
    elif(num == 255):
        return '0'
    temp = 0
    for x in range (playernum):
        temp += round(160/playernum)
        if(temp == num):
            return str(x+1)
    return '-'

    
    


rows, cols = img_array.shape
with open("output.txt", "w") as file:
    file.write(str(playernum))
    file.write("\n")
    while True:
        try:
            value = int(input("Enter the number of overwrite stones: "))
            if(value < 0 | value > 255):
                raise ValueError
            file.write(str(value))
            file.write("\n")
            break  # Exit loop if input is valid
        except ValueError:
            print("That's not a valid integer. Please try again.")

    while True:
        try:
            value = int(input("Enter the number of bombs: "))
            if(value < 0 | value > 255):
                raise ValueError
            file.write(str(value))
            file.write(" ")
            break  # Exit loop if input is valid
        except ValueError:
            print("That's not a valid integer. Please try again.")

    while True:
        try:
            value = int(input("Enter an the desired bomb radius: "))
            if(value < 0 | value > 255):
                raise ValueError
            file.write(str(value))
            file.write("\n")
            break  # Exit loop if input is valid
        except ValueError:
            print("That's not a valid integer. Please try again.")

    file.write(str(len(img_array)))
    file.write(" ")
    file.write(str(len(img_array[1])))
    file.write("\n")


    for i in range(rows):
        for j in range(cols):
            file.write(int_to_char(process(img_array[i][j])))
            if(j != cols-1):
                file.write(" ")
        file.write("\n")

