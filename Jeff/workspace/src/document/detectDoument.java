package document;

import java.util.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

public class detectDoument {
	
	private static String inputPath = "images/inputs/", outputPath = "images/outputs/";
	private static String imageName, originalPath, contourPath, grayPath, gaussianPath;
	
	public static void main(String[] args) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			
			Scanner input = new Scanner(System.in);
			System.out.println("Name of image that needs to be tested >>");
			imageName = input.nextLine();
			originalPath = inputPath + imageName;
			System.out.println("opening " + originalPath);
			File inputImage = new File(originalPath);
			BufferedImage image = ImageIO.read(inputImage);
			System.out.println("image loading completed");
			Mat original = Imgcodecs.imread(originalPath);

			System.out.println("\nstarting grayscaling");
			Mat grayscale = original.clone();
			Imgproc.cvtColor(original, grayscale, Imgproc.COLOR_RGB2GRAY);
			grayPath = outputPath + imageName.substring(0, imageName.length()-4) + "Gray.jpg";
			createFile(grayPath);
			Imgcodecs.imwrite(grayPath, grayscale);
			System.out.println("grayscale conversion completed");

			System.out.println("\nstarting contouring of target");
			Mat blurred = original.clone();
			contourPath = outputPath + imageName.substring(0, imageName.length()-4) + "Contoured.jpg";
			Imgproc.medianBlur(original, blurred, 9);
			Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);
			Mat gray = new Mat();
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			List<Mat> blurredChannel = new ArrayList<Mat>();
			blurredChannel.add(blurred);
			List<Mat> gray0Channel = new ArrayList<Mat>();
			gray0Channel.add(gray0);
			MatOfPoint2f approxCurve;
			double maxArea = 0;
			int maxId = -1;
			for (int c = 0; c < 3; c++) {
				int ch[] = { c, 0 };
				Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
				int thresholdLevel = 1;
				for (int t = 0; t < thresholdLevel; t++) {
					if (t == 0) {
						Imgproc.Canny(gray0, gray, 10, 20, 3, true);
						Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
					} else {
						Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, (original.width() + original.height()) / 200, t);
					}
					Imgproc.findContours(gray, contours, new Mat(),	Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
					for (MatOfPoint contour : contours) {
						MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
						double area = Imgproc.contourArea(contour);
						approxCurve = new MatOfPoint2f();
						Imgproc.approxPolyDP(temp, approxCurve,	Imgproc.arcLength(temp, true) * 0.02, true);
						if (approxCurve.total() == 4 && area >= maxArea) {
							double maxCosine = 0;
							List<Point> curves = approxCurve.toList();
							for (int j = 2; j < 5; j++) {
								double cosine = Math.abs(angle(curves.get(j % 4), curves.get(j - 2), curves.get(j - 1)));
								maxCosine = Math.max(maxCosine, cosine);
							}
							if (maxCosine < 0.3) {
								maxArea = area;
								maxId = contours.indexOf(contour);
							}
						}
					}
				}
			}
			if (maxId >= 0) {
				Imgproc.drawContours(original, contours, maxId, new Scalar(255, 0, 0, 0.8), 8);
			}
			createFile(contourPath);
			Imgcodecs.imwrite(contourPath, original);
			System.out.println("rectangle contouring completed");
			
			System.out.println("retrieving paper rotation");
			

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	private static double angle(Point point1, Point point2, Point point3) {
		double dx1 = point1.x - point3.x;
		double dy1 = point1.y - point3.y;
		double dx2 = point2.x - point3.x;
		double dy2 = point2.y - point3.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}

	private static void createFile(String filePath) throws IOException {
		File output = new File(filePath);
		if(output.createNewFile()) {
			System.out.println("created " + filePath);
		} else {
			System.out.println("overrode " + filePath);
		}
	}
	
}