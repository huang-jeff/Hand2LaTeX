'''
Supporting function to rectify and find the corners of the rectangle
@author: Jeffrey
'''

import numpy as np
import cv2

def cornerPoints(corner):
    corner = corner.reshape((4,2))
    rect = np.zeros((4,2), dtype = np.float32)
    
    add = corner.sum(axis = 1)
    rect[0] = corner[np.argmin(add)]
    rect[2] = corner[np.argmax(add)]
    
    diff = np.diff(corner, axis = 1)
    rect[1] = corner[np.argmin(diff)]
    rect[3] = corner[np.argmax(diff)]
    
    return rect