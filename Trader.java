/* John Rice
   This class makes several trades between districts to try to get to a "fair"
   representation.
*/

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.Math;

public class Trader
{
   private int[][] grid;
   private ArrayList<District> disList;
   private int disPop, totalPerim, highPerim;
   private double repRatio;
   
   public Trader(int[][] grid, ArrayList<District> disList, int disPop)
   {
      this.grid = grid;
      this.disList = disList;
      this.disPop = disPop;
      
      totalPerim = calcPerim(disList);
      //testOne(4);
   }
   
   /*
      Tests to see if the cells bordering the specified distrct are being calculated
      correctly.
   */
   private void testOne(int disNumber)
   {
      District temp = disList.get(disNumber);
      ArrayList<District> borderDistricts = temp.getBorderDistricts(disList);
      for (District dis : borderDistricts)  {
         ArrayList<Point> border = temp.getBorderCells(dis);
         for (Point cell : border)  {
            System.out.println(cell);
         }
         System.out.println();
      } 
   } 
   
   private int calcPerim(ArrayList<District> disList)
   {
      int temp = 0;
      
      for (District dis : disList)  {
         temp += dis.getPerim();
      }
      return temp;
   }
   
   public void makeTrade()
   {

   }
}
