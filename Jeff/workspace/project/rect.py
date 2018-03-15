'''
Supporting function to rectify and find the area of the rectangle
@author: Jeffrey
'''

import numpy as np

def rectify(x):
    x = x.reshape((4,2))
    xnew = np.zeros((4,2), dtype = np.float32)
    
    add = x.sum(1)
    xnew[0] = x[np.argmin(add)]
    xnew[2] = x[np.argmax(add)]
    
    diff = np.diff(x, axis = 1)
    xnew[1] = x[np.argmin(diff)]
    xnew[3] = x[np.argmax(diff)]
    
    return xnew