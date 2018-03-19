from PIL import Image, ImageFilter, ExifTags
import os
import argparse
import sys


#pytesseract.pytesseract.tesseract_cmd = "C:\\Program Files (x86)\\Tesseract-OCR\\tesseract"
def cleanFile(filePath, newFilePath):
    img = Image.open(filePath)
    #im = im.point(lambda x:0 if x<143 else 255)
    gray = img.convert('L')
    bw = gray.point(lambda x: 0  if x<160 else 250, '1')
    bw.save(newFilePath, "PPM")
    bw.show()


#parser = argparse.ArgumentParser()
#args = parser.parse_args()
cleanFile(sys.argv[-1], "cleaned.ppm")