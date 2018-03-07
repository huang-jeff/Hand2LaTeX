package test;

import java.util.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

public class detectDoument {
	
	private static String grayPath;
	
	public static void main(String[] args) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);	
			Scanner input = new Scanner(System.in);
			System.out.println("Name of image that needs to be tested >>");
			String inputName = "images/" + input.nextLine();
			System.out.println("opening " + inputName);
			File inputImage = new File(inputName);
			BufferedImage image = ImageIO.read(inputImage);
			System.out.println("image loading completed");
			byte[] originalData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			Mat original = Imgcodecs.imread(inputName);
			original.put(0, 0, originalData);
			
			Mat grayscale = original.clone();
			Imgproc.cvtColor(original, grayscale, Imgproc.COLOR_RGB2GRAY);
			
			byte[] grayData = new byte[grayscale.rows() * grayscale.cols() * (int)(grayscale.elemSize())];
			grayscale.get(0, 0, grayData);
			BufferedImage grayImage = new BufferedImage(grayscale.cols(), grayscale.rows(), BufferedImage.TYPE_BYTE_GRAY);
			grayImage.getRaster().setDataElements(0,  0 , grayscale.cols(), grayscale.rows(), grayData);
			
			grayPath = inputName.substring(0,inputName.length()-4) + "Gray.jpg";
			File output = new File(grayPath);
			if(output.createNewFile()) {
				System.out.println("created " + grayPath);
			} else {
				System.out.println("overrode " + grayPath);
			}
			ImageIO.write(grayImage, "jpg", output);
			System.out.println("grayscale conversion completed");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
}