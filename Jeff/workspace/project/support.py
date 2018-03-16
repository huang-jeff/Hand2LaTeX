'''
Supporting function for document scanner

cornerPoints(image) - locating the corners of the document in question in an image
findName(string) - extracting file name without file extension to output in a unique folder
perspectiveTransform(image, list) - extract the location of the corner points and set thme as the base coordinates
@author: Jeffrey
'''

from PIL import Image
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
    
def downscale_image(image, max_dim=2048):
    a, b = image.size
    if max(a, b) <= max_dim:
        return 1.0, image
    scale = 1.0 * max_dim / max(a, b)
    newImage = image.resize((int(a * scale), int(b * scale)), Image.ANTIALIAS)
    return scale, newImage
    
def findBorder(contours, arr):
    borders = []
    area = arr.shape[1]
    for i, c in enumerate(contours):
        x, y, w, h = cv2.boundingRect(c)
        if w * h > 0.5 * area:
            borders.append((i, x, y, x + 2 - 1, y + h - 1))
    return borders

def removeBorder(contour, arr):
    conImage = np.zeros(arr.shape)
    rem = cv2.minAreaRect(contour)
    degrees = rem[2]
    if min(degrees % 90, 90 - (degrees % 90)) <= 10.0:
        box = cv2.boxPoints(rem)
        box = np.int0(box)
        cv2.drawContours(conImage, [box], 0, 255, -1)
        cv2.drawContours(conImage, [box], 0, 0, 4)
    else:
        x1, y1, x2, y2 = cv2.boundingRect(contour)
        cv2.rectangle(conImage, (x1, y1), (x2, y2), 255, -1)
        cv2.rectangle(conImage, (x1, y1), (x2, y2), 0, 4)
    return np.minimum(conImage, arr)

def dilate(arr, N, iterations):
    kernel = np.zeros((N, N), dtype=np.uint8)
    kernel[(N-1)//2,:] = 1
    dilateImage = cv2.dilate(arr/255, kernel, iterations = iterations)
    kernel = np.zeros((N, N), dtype=np.uint8)
    kernel[:,(N-1)//2] = 1
    dilateImage = cv2.dilate(dilateImage, kernel, iterations = iterations)
    return dilateImage

def findComponents(edges, maxComps=16):
    count = 21
    dilation = 5
    n = 1
    while count > 16:
        n += 1
        dilateImage = dilate(edges, N = 3, iterations = n)
        dilateImage = np.uint8(dilateImage)
        _, contours, heirarchy = cv2.findContours(dilateImage, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
        count = len(contours)
    return contours

def contourAssist(contours, arr):
    cInfo = []
    for c in contours:
        x, y, w, h = cv2.boundingRect(c)
        cImage = np.zeros(arr.shape)
        cv2.drawContours(cImage, [c], 0, 255, -1)
        cInfo.append({
            'x1': x,
            'y1': y,
            'x2': x + w -1,
            'y2': y + h -1,
            'sum': np.sum(arr * (cImage > 0))/255
        })
    return cInfo

