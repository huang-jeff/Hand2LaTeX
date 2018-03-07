package latex;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class Example extends JFrame {

	
	//used jLatexMath to generate a tex equation.
	//used swing to display - see if it works
	//if given a text file with a string then can alter code to generation a tex 
	// there is a python libriary called pylatex and PyTeX that does something similar - chose to look into java cause right now we're sticking everything to java.
	
	public Example() throws HeadlessException {
		super();
		  this.setTitle("Latex example");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(200, 200, 800, 800);
	}

	public static void main(String[] args) {
		
		
		
Example app = new Example();
		
		String equ = "\\frac {V_m} {K_M+S}";

		TeXFormula formula = new TeXFormula(equ);
		TeXIcon ti = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 40);
		BufferedImage b = new BufferedImage(ti.getIconWidth(), ti.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		 
		ti.paintIcon(new JLabel(), b.getGraphics(), 0, 0);
		 
		JPanel mainPanel = new JPanel();
		JLabel fLabel = new JLabel();
		fLabel.setIcon(ti);
		mainPanel.add(fLabel);
		app.add(mainPanel);
		app.setVisible(true);
		app.pack();
		
		
		
		
		
		
		
		
		
		

	}

}
