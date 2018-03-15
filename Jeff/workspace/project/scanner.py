'''
Document scanner for extracting rectangular objects from a given image.
@author: Jeffrey
'''

import cv2
import numpy as np
import os.path

import rect

inputPath = '../images/inputs/'
outputPath = '../images/outputs/'

imageName = raw_input("Enter image name: ")
imagePath = inputPath + str(imageName)
print('importing >> ' + imagePath)


if os.path.exists(imagePath):
    print('file found')
    original = cv2.imread(imagePath)
    original = cv2.resize(original, (1500, 880))
    print('image read')
    
    backupOriginal = original.copy()
    
    grayscale = cv2.cvtColor(original, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grayscale, (5, 5), 0)
    
    edged = cv2.Canny(blurred, 0, 50)
    backupEdged = edged.copy()
    
    (contours, _) = cv2.findContours(edged, cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)
    contours = sorted(contours, key=cv2.contourArea, reverse=True)
    
    for c in contours:
        p = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * p, True)
        
        if len(approx) == 4:
            target = approx 
            break
        
    approx = rect.cornerPoints(target)
    pts2 = np.float32([[0,0],[800,0],[800,800],[0,800]])
    
    M = cv2.getPerspectiveTransform(approx,pts2)
    dest = cv2.warpPerspective(backupOriginal,M,(800,800))
    
    cv2.drawContours(original, [target], -1, (0, 255, 0), 2)
    dest = cv2.cvtColor(dest, cv2.COLOR_BGR2GRAY)
    
    ret, threshold1 = cv2.threshold(dest, 127, 255, cv2.THRESH_BINARY)
    threshold2 = cv2.adaptiveThreshold(dest, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 11, 2)
    threhold3 = cv2.adaptiveThreshold(dest, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)
    ret2, threshold4 = cv2.threshold(dest, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    
    print('outputting original ... done')
    cv2.imwrite(outputPath + 'original_' + imageName, backupOriginal)
    print('outputting grayscale ... done')
    cv2.imwrite(outputPath + 'grayscale_' + imageName, grayscale)
    print('outputting blurred original ... done')
    cv2.imwrite(outputPath + 'blurred_original_' + imageName, blurred)
    print('outputting edged original ... done')
    cv2.imwrite(outputPath + 'edged_original_' + imageName, backupEdged)
    print('outputting outline ... done')
    cv2.imwrite(outputPath + 'outline_' + imageName, original)
    print('outputting threshold binary ... done')
    cv2.imwrite(outputPath + 'threshold_binary_' + imageName, threshold1)
    
else:
    print('file not found')