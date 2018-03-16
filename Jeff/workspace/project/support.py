'''
Supporting function for document scanner

cornerPoints(image) - locating the corners of the document in question in an image
findName(string) - extracting file name without file extension to output in a unique folder
@author: Jeffrey
'''

import numpy as np
import cv2
from imutils.perspective import order_points

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

def findName(name):
    indType = name.find('.')
    nameOnly = name[0:indType] + '/'
    return nameOnly

def perspectiveTransform(image, points):
    rect = order_points(points)
    (topLeft, topRight, bottomRight, bottomLeft) = rect
    
    widthA = np.sqrt(((bottomRight[0] - bottomLeft[0]) ** 2) + ((bottomRight[1] - bottomLeft[1]) ** 2))
    widthB = np.sqrt(((topRight[0] - topLeft[0]) ** 2) + ((topRight[1] - topLeft[1]) ** 2))
    maxWidth = max(int(widthA), int(widthB))
    
    heightA = np.sqrt(((topRight[0] - bottomRight[0]) ** 2) + ((topRight[1] - bottomRight[1]) ** 2))
    heightB = np.sqrt(((topLeft[0] - bottomLeft[0]) ** 2) + ((topLeft[1] - bottomLeft[1]) ** 2))
    maxHeight = max(int(heightA), int(heightB))
    
    
    destinationPoints = np.array([
        [0,0],
        [maxWidth - 1, 0],
        [maxWidth - 1, maxHeight - 1],
        [0, maxHeight - 1]],
        dtype = "float32")
    
    M = cv2.getPerspectiveTransform(rect,destinationPoints)
    warped = cv2.warpPerspective(image, M, (maxWidth, maxHeight))
    
    return warped
    
    
