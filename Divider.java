/* John Rice
   This class determines how the districts are divided.
*/

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.Math;

public class Divider
{
   private int[][] grid;
   private ArrayList<District> disList;
   private int numDis, pop, disPop, totalPerim = 0, highPerim = 0;
   private double repRatio, blu, red;
   
   public Divider(int[][] grid, int numDis, int numSquare)
   {
      this.grid = grid;
      this.numDis = numDis;
      this.pop = numSquare;
      this.disPop = pop / numDis;
      
      //initDistricts();
      snakeInit();
      setRatio();
      
      double blue = 0;
      for (int ndxA = 0; ndxA < grid.length; ndxA++)  {
		   for (int ndxB = 0; ndxB < grid[ndxA].length; ndxB++)   {	      
		      if (grid[ndxA][ndxB] == 1)  {
		         blue++;
		      }
		   }
      }
      
      blu = 100 * (blue / pop);
      red = 100 - blu;
      System.out.println((int) blu + "% blue, " + (int) red + "% red");
   }
   
   public void initDistricts()
   {
      disList = new ArrayList<District> ();
      
      for (int ndx = 0; ndx < numDis; ndx++) {
         ArrayList<Point> zone = new ArrayList<Point> ();
         for (int i = ndx * disPop; i < (ndx + 1) * disPop; i++) {
            int y = i / grid.length, x = i % grid.length;
            if (y % 2 == 1)   {
               x = grid.length - x - 1;
            }
            zone.add(new Point(x, y));
         }
         
         disList.add(new District(grid, zone));
      }
   }
   
   private void snakeInit()
   {
      int width = (int) Math.round(Math.sqrt(disPop));
      int remain = grid.length % width, numRex = grid.length / width;
      int[] widths = new int[numRex + 1];
      
      // Deals with a special case where an odd remainder can mess things up
      if (width % 2 == 0 && remain % 2 == 1) {
         if (Math.ceil(Math.sqrt(disPop)) % 2 == 1)   {
            width = (int) Math.ceil(Math.sqrt(disPop));
         }
         else if (((int) Math.sqrt(disPop)) % 2 == 1)   {
            width = (int) (Math.sqrt(disPop));
         }
         else  {
            width++;
         }
         remain = grid.length % width;
         numRex = grid.length / width;
         widths = new int[numRex + 1];
      }
         
      for (int ndx = 0; ndx < numRex; ndx++) {
         widths[ndx] = width;
      }
      widths[numRex] = remain;
      
      // Corrects for a small remainder.
      if (remain <= width / 2)   {
         widths[numRex - 1] += remain;
         widths[numRex] = 0;
      }
      
      // Choose the pattern to snake in
      if (grid[0].length % 2 == 1)  {
         oddSnake(widths);
      }
         
      else if (width % 2 == 1) {
         evenOddSnake(widths);
      }
      else  {
         evenEvenSnake(widths);
      }
   }  
   
   private void evenEvenSnake(int[] widths)
   {
      disList = new ArrayList<District> ();    
      int disCount = 0, j = 0, i = 0, startX = 0;
      ArrayList<Point> zone = new ArrayList<Point> ();
      String lastMove = "right";
     
      for (int ndx = 0; disList.size() < numDis; ndx++)   {
         startX = j;
         if (ndx % 2 == 0) {
            while (i < grid[0].length - 2)  {
               zone.add(new Point(j, i));
               if (i % 2 == 0)   {
                  j++;
               }
               else  {
                  j--;
               }
               if (j == startX + widths[ndx])   {
                  i++;
                  j--;
               }
               else if (j == startX - 1) {
                  i++;
                  j++;
               }
               
               if (zone.size() == disPop)  {
                  disList.add(new District(grid, zone));
                  zone = new ArrayList<Point> ();
               }
            }
            
            lastMove = "right";
            while (j < startX + widths[ndx])   {
               zone.add(new Point(j, i));
                                 
               if ((i == grid[0].length - 2 || i == 0) && lastMove.equals("right"))  {
                  i++;
                  lastMove = "down";
               }
               else if (((i == grid[0].length - 2 || i == 0) && lastMove.equals("up")) ||
                ((i == grid[0].length - 1 || i == 1) && lastMove.equals("down")))   {
                  j++;
                  lastMove = "right";
               }
               else if ((i == grid[0].length - 1 || i == 1) && lastMove.equals("right"))   {
                  i--;
                  lastMove = "up";
               }
               if (zone.size() == disPop)  {
                  disList.add(new District(grid, zone));
                  zone = new ArrayList<Point> ();
               }
            }  
         }
         
         else  {
            lastMove = "right";
            while (j < startX + widths[ndx])   {
               zone.add(new Point(j, i));
                                 
               if ((i == grid[0].length - 2 || i == 0) && lastMove.equals("right"))  {
                  i++;
                  lastMove = "down";
               }
               else if (((i == grid[0].length - 2 || i == 0) && lastMove.equals("up")) ||
                ((i == grid[0].length - 1 || i == 1) && lastMove.equals("down")))   {
                  j++;
                  lastMove = "right";
               }
               else if ((i == grid[0].length - 1 || i == 1) && lastMove.equals("right"))   {
                  i--;
                  lastMove = "up";
               }
               if (zone.size() == disPop)  {
                  disList.add(new District(grid, zone));
                  zone = new ArrayList<Point> ();
               }
            }  
            i--;
            j--;
            
            while (i >= 0)  {
               zone.add(new Point(j, i));                 
               if (i % 2 == 1)   {
                  j--;
               }
               else  {
                  j++;
               }
               if (j == startX + widths[ndx])   {
                  i--;
                  j--;
               }
               else if (j == startX - 1) {
                  i--;
                  j++;
               }
               if (zone.size() == disPop)  {
                  disList.add(new District(grid, zone));
                  zone = new ArrayList<Point> ();
               }
            }
            i++;
            j++;
         }
      }      
   
   }
   
   private void evenOddSnake(int[] widths)
   {
      disList = new ArrayList<District> ();    
      int disCount = 0, j = 0, i = 0, startX;
      ArrayList<Point> zone = new ArrayList<Point> ();
     
      for (int ndx = 0; disList.size() < numDis; ndx++)   {
         startX = j;
         while ((i < grid[0].length - 2 && ndx % 2 == 0) || (i > 1 && ndx % 2 == 1))  {
            zone.add(new Point(j, i));
            if ((i % 2 == 0 && ndx % 2 == 0) || (i % 2 == 1 && ndx % 2 == 1))   {
               j++;
            }
            else  {
               j--;
            }
            if (j == startX + widths[ndx])   {
               if (ndx % 2 == 0) {
                  i++;
               }
               else  {
                  i--;
               }
               j--;
            }
            else if (j == startX - 1) {
               if (ndx % 2 == 0) {
                  i++;
               }
               else  {
                  i--;
               }
               j++;
            }
            if (zone.size() == disPop)  {
               disList.add(new District(grid, zone));
               zone = new ArrayList<Point> ();
            }
         }
         
         // This deals with the problem of the last two rows of the snake
         String lastMove = "right";
         while (j < startX + widths[ndx])   {
            zone.add(new Point(j, i));       
            if ((i == grid[0].length - 2 || i == 0) && lastMove.equals("right"))  {
               i++;
               lastMove = "down";
            }
            else if (((i == grid[0].length - 2 || i == 0) && lastMove.equals("up")) ||
             ((i == grid[0].length - 1 || i == 1) && lastMove.equals("down")))   {
               j++;
               lastMove = "right";
            }
            else if ((i == grid[0].length - 1 || i == 1) && lastMove.equals("right"))   {
               i--;
               lastMove = "up";
            }
            
            if (zone.size() == disPop)  {
               disList.add(new District(grid, zone));
               zone = new ArrayList<Point> ();
            }
         }
      }          
   }
   
   private void oddSnake(int[] widths)
   {
      disList = new ArrayList<District> ();    
      int disCount = 0, j = 0, i = 0;
      ArrayList<Point> zone = new ArrayList<Point> ();
     
      for (int ndx = 0; disList.size() < numDis; ndx++)   {
         int startX = j;
         
         while ((i < grid[0].length && ndx % 2 == 0) || (i >= 0 && ndx % 2 == 1))  {
            if (disCount == 0)   {
               zone = new ArrayList<Point> ();
            }
            zone.add(new Point(j, i));
            disCount++;
                
            if (i % 2 == 0)   {
               j++;
            }
            else  {
               j--;
            }
            if (j == startX + widths[ndx])   {
               if (ndx % 2 == 0) {
                  i++;
               }
               else  {
                  i--;
               }
               j--;
            }
            else if (j == startX - 1) {
               if (ndx % 2 == 0) {
                  i++;
               }
               else  {
                  i--;
               }
               j++;
            }
            if (disCount == disPop)  {
               disList.add(new District(grid, zone));
               disCount = 0;
            }
         }
         if (ndx % 2 == 0) {
            i--;
         }
         else  {
            i++;
         }
         j++;
      }          
   }
      
   /*
      Updates the ratio of Blue representatives relative to the number of districts.
   */
   public void setRatio()
   {
      double numB = 0, numR = 0;
      
      for (District d1 : disList)   {
         if (d1.getRep().equals("Blue"))  {
            numB++;
         }
         else if (d1.getRep().equals("Red")) {
            numR++;
         }
         else   {
            numB += .5;
            numR += .5;
         }
         repRatio = numB / disList.size();
      }
   }
   
   public void drawBounds(Graphics2D g2, int rexSize)
   {
      totalPerim = 0;
      for (District d1 : disList)   {
         //System.out.println(d1);
         int newPerim = d1.drawDis(g2, rexSize);
         if (highPerim < newPerim)  {
            highPerim = newPerim;
         }
         totalPerim = totalPerim + newPerim;
      }
   }
   
   public String toString()
   {
      setRatio();
      int ratioB = (int) (repRatio * 100), ratioR = 100 - ratioB;
      
      return ""  + ratioB + "% blue representation, " + ratioR + "% red representation"
       + "\nPerimeter: " + totalPerim + "\nAverage Perimeter: " + totalPerim / numDis
       + "\nHighest Perimeter: " + highPerim;
   }
   
   public ArrayList<District> getDisList()
   {
      return disList;
   }
   
   // Returns the proportion of blue voters.
   public double getPopRatio()
   {
      return blu / 100;
   }
   
   // Returns the proportion of blue representatives.
   public double getRepRatio()
   {
      setRatio();
      return repRatio;
   }
} 
   
   
