import math
import statistics
from PIL import Image
import numpy as np

#get input
image = Image.open("images/test.png")

img_array = np.array(image)
res_array = np.zeros(shape=(img_array.shape), dtype=np.uint8)
radius = 2
spatialparameter = 35
rangeparameter = 45


def medianfilter():
    for i in range(len(img_array)):
        for j in range(len(img_array[i])):
            res_array[i][j] = medianfilterpixel(i, j)

def medianfilterpixel(i, j):
    arr = []
    neighborhood = getneighborhood(i, j)
    for x in range(radius*2+1):
        for y in range(radius*2+1):
            arr.append(neighborhood[x][y])
    return statistics.median(arr)

def getneighborhood(i, j):
    res = np.empty(shape=(radius*2+1, radius*2+1), dtype=np.uint8)
    k = i-radius
    l = j-radius
    n = 0
    mean = 0
    while(k <= i+radius):
        while(l <= j+radius):
            if(k >= 0 and l >= 0 and k < len(img_array) and l < len(img_array[k])):
                res[k - (i - radius)][l - (j - radius)] = img_array[k][l]
                mean += img_array[k][l]
                n += 1
            l += 1

        l = j-radius
        k += 1
    
    mean = mean/n

    for x in range(radius*2+1):
        for y in range(radius*2+1):
            if not (res[x,y]):
                res[x][y] = mean

    
    return res

def d(i,j,k,l):
    
    return math.exp(-((math.pow(i-k,2) + math.pow(j-l,2))/(2*math.pow(spatialparameter,2))))

def r(i,j,k,l, array):
    
    return math.exp(-((math.pow(array[i][j]-array[k,l],2))/(2*math.pow(rangeparameter,2))))

def w(i,j,k,l, array):

    return d(i,j,k,l)*r(i,j,k,l, array)

def g(i,j):
    neighborhood = getneighborhood(i,j)
    sum1 = 0
    sum2 = 0
    i = radius+1
    j = radius+1
    for k in range(len(neighborhood)):
        for l in range(len(neighborhood[k])): 
            sum1 += neighborhood[k][l] * w(i,j,k,l, neighborhood)

    for k in range(len(neighborhood)):
        for l in range(len(neighborhood[k])): 
            sum2 += w(i,j,k,l, neighborhood)
    
    return sum1/sum2
    
def bilateralfilter():
    for i in range(len(img_array)):
        for j in range(len(img_array[i])):
            res_array[i][j] = g(i,j)


bilateralfilter()

    


    



#save output
im = Image.fromarray(res_array)
im.save("images/output.png")
