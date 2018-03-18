from PIL import Image, ImageFilter, ExifTags
import cv2
import os
import argparse
from scipy import misc, ndimage
import numpy as np
import sys
import subprocess
import pytesseract
import matplotlib.pyplot as plt
import pathlib


#pytesseract.pytesseract.tesseract_cmd = "C:\\Program Files (x86)\\Tesseract-OCR\\tesseract"
def cleanFile(filePath, newFilePath):
    img = Image.open(filePath)
    #im = im.point(lambda x:0 if x<143 else 255)
    gray = img.convert('L')
    bw = gray.point(lambda x: 0  if x<120 else 250, '1')
    bw = bw.rotate(90, expand=1)
    bw.save(newFilePath)
    bw.show()
def vectorize(pic):
    fimg = cv2.imread(pic)
  #  print(pytesseract.image_to_string(fimg))
    plt.axis("off")
    plt.imshow(fimg)
    plt.savefig("fig.svg")
    plt.show()


#parser = argparse.ArgumentParser()
#args = parser.parse_args()
cleanFile(sys.argv[-1], "cleaned.JPG")
vectorize("cleaned.JPG")
