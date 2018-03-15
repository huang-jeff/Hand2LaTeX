package test;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.util.*;

public class HelloCV {

	public static void main(String[] args) {
		System.out.println(">>> Matrix dump <<<");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		System.out.println("mat = " + mat.dump());
		
		Scanner input = new Scanner(System.in);
		System.out.println("Select picture for analysis: ");
		
	}

}
