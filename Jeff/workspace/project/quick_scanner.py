'''
Document scanner for extracting rectangular objects from a given image. no output values or files. just displays
as a window that will be destoryed when a key is pressed.
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
    
print('importing from >> ' + imagePath)
print('   output path >> ' + outputPath)

if os.path.exists(imagePath):
    print('file found\n')
    
    image = cv2.imread(imagePath)
    ratio = image.shape[0] / 500.0
    original = image.copy()
    image = imutils.resize(image, height = 500)
    
    grayscale = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    grayscale = cv2.GaussianBlur(grayscale, (5, 5), 0)
    edged = cv2.Canny(grayscale, 75, 200)
    
    (_, cnts, _) = cv2.findContours(edged.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    cnts = sorted(cnts, key = cv2.contourArea, reverse = True)[:10]
    screenCnt = None
    
    for c in cnts:
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)
        
        if len(approx) == 4:
            screenCnt = approx
            break
        
    cv2.drawContours(image, screenCnt, -1, (0, 255, 0), 5)
    
    cv2.imshow('Image', image)
    cv2.imshow('Edged', edged)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    
else:
    print('file not found')
    
print('end')