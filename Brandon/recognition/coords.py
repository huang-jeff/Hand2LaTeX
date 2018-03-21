from xml.dom import minidom
from svg.path import Path, Line, Arc, CubicBezier, QuadraticBezier
from svg.path import parse_path
from PIL import Image, ImageFilter, ExifTags
import os
import subprocess
import argparse
import sys
import numpy as np
import math, cmath
import re


def cleanFile(filePath, newFilePath):
    img = Image.open(filePath)
    #im = im.point(lambda x:0 if x<143 else 255)
    img = img.filter(ImageFilter.GaussianBlur(radius=2))
    gray = img.convert('L')
    bw = gray.point(lambda x: 0  if x<160 else 250, '1')

    bw.save(newFilePath,"ppm")




#parser = argparse.ArgumentParser()
#args = parser.parse_args()
cleanFile(sys.argv[-1], "cleaned.ppm")

subprocess.call(["autotrace",   "--output-format", "eps", "--output-file", "output.eps",  "--centerline", "--filter-iterations", "20", "--corner-threshold", "120", "--despeckle-level", "15",  "cleaned.ppm"])
subprocess.call(["inkscape", "output.eps", "-l", "output.svg"])

doc = minidom.parse('output.svg')  # parseString also exists
path_strings = [path.getAttribute('d') for path
                in doc.getElementsByTagName('path')]
height = float([path.getAttribute('height') for path in doc.getElementsByTagName('svg')][0])
width = float([path.getAttribute('width') for path in doc.getElementsByTagName('svg')][0])
scale_raw = [path.getAttribute('transform') for path
                in doc.getElementsByTagName('g')]
scale = 1
try:
	for maybe in scale_raw:
		if 'scale' in maybe:
			scale = float(re.sub(r's.*,', '', maybe).replace(')',''))
			height /= scale
			break
except:
	print("No scale present")

def dist_f(x,y):   
    return np.sqrt(np.sum((x-y)**2))
points = []
ends = []
for path in path_strings:
	path_parse = parse_path(path)
	length = path_parse.length(error=1e-5)
	if length < (max(width / 500, 20)):
		continue
	num_points = 200
	point_arr = [[int(round(path_parse.point(x).real)),int(round(height - path_parse.point(x).imag))]  for x in np.linspace(0, 1, num_points)]
	ee = [point_arr[0], point_arr[-1]]
	combined = False
	# combining in loop
	for n, p in enumerate(ends):
			for i,e1 in enumerate(p):
				for j,e2 in enumerate(ee):
					if not combined:
						dist = dist_f(np.asarray(e1),np.asarray(e2))
						if dist < width / 7:
							if(i == 1):
								if(j == 0):
									points[n] = points[n] + point_arr
								else:
									points[n] = points[n] + point_arr[::-1]
							else:
								if(j == 0):
									points[n] = point_arr[::-1] + points[n]
								else:
									points[n] = point_arr + points[n]
							combined = True
							ends[n] = [points[n][0], points[n][-1]]
	if not combined:
		points.append(point_arr)
		ends.append(ee)
combined = 0
# 2nd pass over all ends to combine
for m in range(len(ends)):
	min_dist = None
	for n1, p in enumerate(ends[m + 1:]):
			n = n1 + m + 1
			for i,e1 in enumerate(p):
				for j,e2 in enumerate(ends[m]):
					dist = dist_f(np.asarray(e1),np.asarray(e2))
					if min_dist != None and dist < min_dist[0]:
						min_dist = [dist, n, i, j]
					elif min_dist is None:
						min_dist = [dist, n, i, j]
	if min_dist is not None and min_dist[0] < width / 7:
		combined += 1
		i = min_dist[2]
		j = min_dist[3]
		n = min_dist[1]
		if(i == 1):
			if(j == 0):
				points[m] = points[n] + points[m]
			else:
				points[m] = points[n] + points[m][::-1]
		else:
			if(j == 0):
				points[m] = points[m][::-1] + points[n]
			else:
				points[m] = points[m] + points[n]
		ends[m] = [points[m][0], points[m][-1]]
		del ends[n]
		del points[n]

F = open("out.scgink","w") 
F.write("SCG_INK\n")
F.write(str(len(points)) + "\n")
for p in points:
	F.write(str(len(p)) + "\n")
	for pp in p:
		# pass
		F.write(str(pp[0]) + " " + str(pp[1]) + "\n")
F.close()
doc.unlink()

subprocess.call(["./seshat", "-c", "Config/CONFIG", "-i", "out.scgink", "-o", "out.inkml", "-r", "render.pgm", "-d", "out.dot"])