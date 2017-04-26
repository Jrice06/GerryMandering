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
   private double[][] grid, popGrid;
   private double totalPop = 0;
   private Random rand = new Random ();
   private int rexSize;
   private Divider divider;
   private Trader trade;
   private ArrayList<District> disList;
   		
	public GerryComponent(int width, int height, int numDis, String datafile)
	 throws FileNotFoundException
	{
	   grid = new double[width][height];
	   popGrid = new double[width][height];
	   rexSize = (int) (FRAME_HEIGHT / (Math.max(width, height) + 2));
	   
	   if (datafile.equals(""))  {
         for (int ndxA = 0; ndxA < grid.length; ndxA++)  {
		      for (int ndxB = 0; ndxB < grid[ndxA].length; ndxB++)   {
		         grid[ndxA][ndxB] = getRandomVal();
		      }
		   }
		}
		
		else  {
	      File file = new File(datafile);
	      Scanner in = new Scanner(file);
	      for (int ndxA = 0; ndxA < height; ndxA++)  {
	         for (int ndxB = 0; ndxB < width; ndxB++) {
	            double val = in.nextDouble();
	            grid[ndxB][ndxA] = val;
	         }
	      }
	      	for (int ndxA = 0; ndxA < height; ndxA++)  {
	         for (int ndxB = 0; ndxB < width; ndxB++) {
	            double val = in.nextDouble();
	            popGrid[ndxB][ndxA] = val;
	            totalPop += val;
	         }
	      }
	      if (in.hasNextDouble()) {
	         System.out.println("Error in File Format");
	      }
	   }
	   
	   divider = new Divider(grid, popGrid, numDis, totalPop);
	   this.disList = divider.getDisList();
	   trade = new Trader(grid, popGrid, disList, totalPop / numDis,
	    divider.getPopRatio());
	   //cleanUpGrid();
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
	
	public void manySolve()
	{
	   trade.updateRepRatio(divider.getRepRatio());
	   trade.manySolve();
	}
	
	public void reset()
	{
		divider = new Divider(grid, popGrid, disList.size(), totalPop);
	   this.disList = divider.getDisList();
	   	trade = new Trader(grid, popGrid, disList, totalPop / disList.size(),
	    divider.getPopRatio());
	   	cleanUpGrid();
	}
	
	public double getRandomVal()
	{
	   /*int val = rand.nextInt(3) + 1;
	   
	   if (val == 3)  {
	      val = 2;
	   }
	   return val;*/
	   
	   // Uncomment to make the choice 50-50
	   return rand.nextDouble();
	}
	
	/**
	   This function is called after the grid has been initialized to make any trades
	   which reduce overall perimeter.
	*/
	private void cleanUpGrid()
	{
      for (District dis : disList)  {
         for (District border : dis.getBorderDistricts(disList))   {
            while (trade.cleanUpTrade(dis, border, true))  {
	            ;
	         }
	      }
	   }
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		for (int ndxA = 0; ndxA < grid.length; ndxA++)  {
		   for (int ndxB = 0; ndxB < grid[ndxA].length; ndxB++)   {
		      //float alpha = (float) Math.min(1.0, popGrid[ndxA][ndxB] * numSquares() / totalPop);
		      float alpha = (float) 1.0;
		      float red = (float) (1 - grid[ndxA][ndxB]);
		      float green = (float) (grid[ndxA][ndxB]); 
		      	      
            g2.setColor(new Color(red, (float) 0.0, green, alpha));
		      g2.fill(new Rectangle(rexSize * (ndxA + 1), rexSize * (ndxB + 1),
		       rexSize, rexSize));
		      	g2.setColor(Color.black);
		      g2.draw(new Rectangle(rexSize * (ndxA + 1), rexSize * (ndxB + 1), rexSize,
		       rexSize));
		   }
		}
		divider.drawBounds(g2, rexSize);
		System.out.println(divider);
		for (District dis: disList)   {
		   System.out.println(dis);
		}   
	   System.out.println("-----------------------------------------------------");
	}
	
	private int numSquares()
	{
	   int ret = 0;
	   
	   for (int i = 0; i < popGrid.length; i++)  {
	      for (int j = 0; j < popGrid[0].length; j++)  {
	         if (popGrid[i][j] > 0.001) {
	            ret++;
	         }
	      }
	   }
	   return ret;
	}
}
