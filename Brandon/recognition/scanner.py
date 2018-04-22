'''
Document scanner for extracting rectangular objects from a given image.
@author: Jeffrey
'''

import cv2
import numpy as np
import os.path
import coords
import support
import pickle
import classify
import random
import imutils
import extractletter
from skimage.filters import threshold_local
import sys, traceback
from PIL import Image
from scipy.ndimage.filters import rank_filter
#================================================================================================================
pageExtracted = False
textExtractPath = None

scaling = 500

image = cv2.imread('nice.jpg')
original = image.copy()
origHeight, origWidth, _ = image.shape
ratio = image.shape[0] / scaling
image = imutils.resize(image, height=scaling)
newHeight, newWidth, _ = image.shape
print('starting page extraction ...')
grayscale = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
blurred = cv2.GaussianBlur(grayscale, (5, 5), 0)
edged = cv2.Canny(blurred, 100, 200)
backupEdged = edged.copy()
target = None
(_, contours, _) = cv2.findContours(edged, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
contours = sorted(contours, key=cv2.contourArea, reverse=True)
#    print(contours)
for c in contours:
    p = cv2.arcLength(c, True)
    approx = cv2.approxPolyDP(c, 0.02 * p, True)
    if len(approx) == 4:
        target = approx
        break
try:
    imageArea = np.array([[[newWidth,0]], [[0, 0]], [[0, newHeight]], [[newWidth, newHeight]]], np.int32)
    if target is None:
        target = imageArea
    else:
        area1 = support.polyArea(target)
        area2 = support.polyArea(imageArea)
        if area1 / area2 < 1/3:
            target = imageArea
    print("AREA ",support.polyArea(target))
    print('page found in image')
    print('page found in image')
    pageExtracted = True
    target = support.scaleTarget(target, 0.015)
    newTarget = target * ratio
    #approx = support.cornerPoints(target)
    approx = support.cornerPoints(newTarget)
    print(approx)
    print(approx[0][1] + approx[1][1])
    warpedHeight = int(((approx[3][1]-approx[1][1]) + (approx[2][1] - approx[0][1])) / 2)
    warpedWidth = int(((approx[2][0]-approx[3][0]) + (approx[1][0] - approx[0][0])) / 2)
    print(warpedWidth, warpedHeight)
    pts2 = np.float32([[0,0],[warpedWidth,0],[warpedWidth,warpedHeight],[0,warpedHeight]])

    M = cv2.getPerspectiveTransform(approx,pts2)
    dest = cv2.warpPerspective(original,M,(warpedWidth,warpedHeight))
    destHeight, destWidth, _ = dest.shape
    cv2.drawContours(image, [target], -1, (0, 255, 0), 2)
    dest = cv2.cvtColor(dest, cv2.COLOR_BGR2GRAY)
    
    warped = support.perspectiveTransform(original, target.reshape(4, 2) * ratio)
    warped = cv2.cvtColor(warped, cv2.COLOR_BGR2GRAY)
    T = threshold_local(warped, 11, offset = 10, method = "gaussian")
    warped = (warped > T).astype("uint8") * 255
    
    ret, threshold1 = cv2.threshold(dest, 127, 255, cv2.THRESH_BINARY)
    threshold2 = cv2.adaptiveThreshold(dest, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 11, 2)
    threshold3 = cv2.adaptiveThreshold(dest, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)
    ret2, threshold4 = cv2.threshold(dest, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    
    top, bottom, left, right = [10] * 4
    print('outputting dest ... done')
    dest = cv2.copyMakeBorder(dest, top, bottom, left, right, cv2.BORDER_CONSTANT, value = [0, 0, 0])
    dest = imutils.resize(dest, origHeight)
    cropHeight, cropWidth = dest.shape
    dest = Image.fromarray(dest)
    tImage = dest
except NameError:
    print('page not found in image')
    print(traceback.print_exc(file=sys.stdout))
except:
    print(traceback.print_exc(file=sys.stdout))
print('page extraction ... done')
#================================================================================================================
print('\nstarting text extraction ...')
print("SIZE: ",tImage.size)
scale, image = support.downscale_image(tImage)
edges = cv2.Canny(np.asarray(image), 100, 200)
_, contours, _ = cv2.findContours(edges, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
borders = support.findBorder(contours, edges)
borders.sort(key=lambda i_x1_y1_x2_y2: (i_x1_y1_x2_y2[3] - i_x1_y1_x2_y2[1]) * (i_x1_y1_x2_y2[4] - i_x1_y1_x2_y2[2]))
borderContour = None
if len(borders):
    borderContour = contours[borders[0][0]]
    edges = support.removeBorder(borderContour, edges)
edges = 255 * (edges > 0).astype(np.uint8)
maxed_rows = rank_filter(edges, -2, size=(1, 20))
maxed_cols = rank_filter(edges, -2, size=(20, 1))
debordered = np.minimum(np.minimum(edges, maxed_rows), maxed_cols)
edges = debordered
contours = support.findComponents(edges)
model = classify.load_model('bin')
mapping = pickle.load(open('%s/mapping.p' % 'bin', 'rb'))
if len(contours) == 0:
    print('no text detected')
else:
    crop = support.findOptimalComponents(contours, edges)
    crop = support.padCrop(crop, contours, edges, borderContour)
    crop = [int(x / scale) for x in crop]
    print('outputting detection boxes ... done')
    #support.textDetection(textExtractPath) <-- doesn't work
    #support.textDetection2(textExtractPath) <-- doesn't work
    
    print('outputting text ... done')
    textImage = tImage.crop(crop)
    retImages, image2 = support.textDetection3(textImage, cropWidth, cropHeight)
    prefix = rand_num = random.randint(1, 999999999)
    support.saveImage(image2, prefix)
    svg_paths = []
    document = []
    for ind, line in enumerate(retImages):
        line_pil = Image.fromarray(line['roi'])
        svg_path = coords.makeSVG(line_pil, prefix, ind)
        svg_paths.append({'svg_path': svg_path, 'width': line['width'], 'img': line_pil})
    for p in svg_paths:
        # get a line of text
        if p['width'] < cropWidth / 2:
            document.append(coords.main(p['img']))
        else:    
            document.append(extractletter.main(p['svg_path'], model, mapping))
    document = ("\n").join(document)
    tmp_path = "tmp" + str(prefix) + os.sep
    f = open(tmp_path + "doc.txt", 'w')
    f.write(document)
    f.close()
print ('\nprogram done')
#================================================================================================================


