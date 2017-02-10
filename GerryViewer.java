//John Rice

import javax.swing.JFrame;
import java.io.FileNotFoundException;

public class GerryViewer
{
	public static void main (String[] args) throws FileNotFoundException
	{
	   int width, height, numDistricts;
	   String datafile = "";
	   
	   if (args.length < 3)   {
	      height = width = numDistricts = 10;
		}
		else  {
		   width = Integer.parseInt(args[0]);
		   height = Integer.parseInt(args[1]);
		   numDistricts = Integer.parseInt(args[2]);
		}
		if (args.length == 4)   {
		   datafile = args[3];
		}
		
		JFrame frame = new GerryFrame(width, height, numDistricts, datafile);
			
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		frame.setTitle("Gerrymandering Demonstration");
		frame.setVisible(true);
	}
}
