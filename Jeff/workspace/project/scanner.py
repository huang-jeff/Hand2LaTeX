'''
Document scanner for extracting rectangular objects from a given image.
@author: Jeffrey
'''

import cv2
import numpy as np
import os.path
import imutils
from project import support

inputPath = '../images/inputs/'
outputPath = '../images/outputs/'

imageName = input("Enter image name: ")
imagePath = inputPath + str(imageName)
outputPath = outputPath + support.findName(imageName)
if not os.path.exists(outputPath):
    os.makedirs(outputPath)
    
print(outputPath)

print('importing >> ' + imagePath)

if os.path.exists(imagePath):
    print('file found')
    original = cv2.imread(imagePath)
    original = imutils.resize(original, height=500)
    print('image read')
    backupOriginal = original.copy()
    
    grayscale = cv2.cvtColor(original, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grayscale, (5, 5), 0)
    #blurred = cv2.medianBlur(grayscale, 5)
    
    edged = cv2.Canny(blurred, 75, 200)
    backupEdged = edged.copy()
    
    (_, contours, _) = cv2.findContours(edged, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, key=cv2.contourArea, reverse=True)
    
    for c in contours:
        p = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * p, True)
        
        if len(approx) == 4:
            target = approx 
            break
        
    approx = support.cornerPoints(target)
    pts2 = np.float32([[0,0],[800,0],[800,800],[0,800]])
    
    M = cv2.getPerspectiveTransform(approx,pts2)
    dest = cv2.warpPerspective(backupOriginal,M,(800,800))
    
    cv2.drawContours(original, [target], -1, (0, 255, 0), 2)
    dest = cv2.cvtColor(dest, cv2.COLOR_BGR2GRAY)
    
    ret, threshold1 = cv2.threshold(dest, 127, 255, cv2.THRESH_BINARY)
    threshold2 = cv2.adaptiveThreshold(dest, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 11, 2)
    threshold3 = cv2.adaptiveThreshold(dest, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)
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
    print('outputting threshold mean ... done')
    cv2.imwrite(outputPath + 'threshold_mean_' + imageName, threshold2)
    print('outputting threshold gaussian ... done')
    cv2.imwrite(outputPath + 'threshold_gaussian_' + imageName, threshold3)
    print('outputting otus\'s ... done')
    cv2.imwrite(outputPath + 'otsu_' + imageName, threshold4)
    print('outputting dest ... done')
    cv2.imwrite(outputPath + 'dest_' + imageName, dest)
    
else:
    print('file not found')
