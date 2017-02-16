//John Rice


import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.awt.geom.Ellipse2D;
import java.awt.Color;
import java.util.Random;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.lang.Math;

public class GerryComponent extends JComponent
{
   private static final int FRAME_WIDTH = 800, FRAME_HEIGHT = 800;
   private int[][] grid;
   private Random rand = new Random ();
   private int rexSize;
   private Divider divider;
   private Trader trade;
   private ArrayList<District> disList;
   		
	public GerryComponent(int width, int height, int numDis, String datafile)
	 throws FileNotFoundException
	{
	   grid = new int[width][height];
	   rexSize = (int) (FRAME_HEIGHT / (Math.max(width, height) + 2));
	   
	   if (datafile.equals(""))  {
         for (int ndxA = 0; ndxA < grid.length; ndxA++)  {
		      for (int ndxB = 0; ndxB < grid[ndxA].length; ndxB++)   {
		         grid[ndxA][ndxB] = rand.nextInt(2) + 1;
		      }
		   }
		}
		else  {
	      File file = new File(datafile);
	      Scanner in = new Scanner(file);
	      for (int ndxA = 0; ndxA < height; ndxA++)  {
	         for (int ndxB = 0; ndxB < width; ndxB++) {
	            int val = in.nextInt();
	            grid[ndxB][ndxA] = val;;
	         }
	      }
	      if (in.hasNextInt()) {
	         System.out.println("Error in File Format");
	      }
	   }
	    
	   divider = new Divider(grid, numDis, grid.length * grid[0].length);
	   this.disList = divider.getDisList();
	   trade = new Trader(grid, disList, grid.length * grid[0].length / numDis,
	    divider.getPopRatio());
	}
	
	public void makeTrade()
	{
	   trade.updateRepRatio(divider.getRepRatio());
	   trade.makeBestTrade();
	}
	
	public void undoTrade()
	{
	   	trade.updateRepRatio(divider.getRepRatio());
	   	trade.undoTrade();
	 }
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		for (int ndxA = 0; ndxA < grid.length; ndxA++)  {
		   for (int ndxB = 0; ndxB < grid[ndxA].length; ndxB++)   {	      
		      if (grid[ndxA][ndxB] == 1)  {
		         g2.setColor(Color.blue);
		      }
		      else if (grid[ndxA][ndxB] == 2) {
		         g2.setColor(Color.red);
		      }
		      else  {
		         g2.setColor(Color.black);
		      }
		      
		      g2.fill(new Rectangle(rexSize * (ndxA + 1), rexSize * (ndxB + 1), rexSize, rexSize));
		      	g2.setColor(Color.black);
		      g2.draw(new Rectangle(rexSize * (ndxA + 1), rexSize * (ndxB + 1), rexSize, rexSize));
		   }
		}
		divider.drawBounds(g2, rexSize);
		System.out.println(divider);
	}
}
