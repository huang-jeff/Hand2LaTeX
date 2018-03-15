'''
Document scanner for extracting rectangular objects from a given image.
@author: Jeffrey
'''

import cv2
import numpy as np
import os.path

import rect


imageName = raw_input("Enter image name: ")
imagePath = '../images/inputs/' + str(imageName)
print('importing >> ' + imagePath)


if os.path.exists(imagePath):
    print('file found')
    original = cv2.imread(imagePath)
    original = cv2.resize(original, (1500, 880))
    print('image read')
    
    backup = original.copy()
    
    grayscale = cv2.cvtColor(original, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grayscale, (5, 5), 0)
    
    edged = cv2.Canny(blurred, 0, 50)
    backupEdge = edged.copy()
    
    (contours, _) = cv2.findContours(edged, cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)
    contours = sorted(contours, key = cv2.contourArea, reverse = True)
    
    for c in contours:
        p = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * p, True)
        
        if len(approx) == 4:
            target = approx 
            break
        
    approx = rect.rectify(target)
    pts2 = np.float32([[0,0],[800,0],[800,800],[0,800]])
    
    M = cv2.getPerspectiveTransform(approx,pts2)
    dst = cv2.warpPerspective(backup,M,(800,800))
    
    cv2.drawContours(original, [target], -1, (0, 255, 0), 2)
    dst = cv2.cvtColor(dst, cv2.COLOR_BGR2GRAY)
    
    
else:
    print('file not found')