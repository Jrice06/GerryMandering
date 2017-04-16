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
   private double[][] grid;
   private ArrayList<District> disList;
   private int numDis, pop, disPop, totalPerim = 0, highPerim = 0;
   private double repRatio, blu, red;
   
   public Divider(double[][] grid, int numDis, int numSquare)
   {
      this.grid = grid;
      this.numDis = numDis;
      this.pop = numSquare;
      this.disPop = pop / numDis;
      disList = new ArrayList<District> (); 
      
      takeBestSnake();
      //snakeInit();
      setRatio();
      
      double blue = 0;
      for (int ndxA = 0; ndxA < grid.length; ndxA++)  {
		   for (int ndxB = 0; ndxB < grid[ndxA].length; ndxB++)   {	      
		      blue += grid[ndxA][ndxB];
		   }
      }
      
      blu = 100 * (blue / pop);
      red = 100 - blu;
   }
   
   // Try two versions of the complex snake without remainder and take the
   // result with the lowest (overall perimeter + highest district perimeter).
   private void takeBestSnake()
   {
      int perimV, perimH;
      
      snakeInit();
      perimV = calcPerim() + calcHighPerim();
      boolean isolatedV = gridHasIsolation();
      disList = new ArrayList<District> (); 
      
      reflectGrid();
      snakeInit();
      reflectGrid();
      perimH = calcPerim() + calcHighPerim();
      
      System.out.print("Vertical Snake: " + perimV);
      System.out.println("   Horizontal Snake: " + perimH);
      if ((perimV < perimH && !isolatedV) || gridHasIsolation()) {
         disList = new ArrayList<District> (); 
         snakeInit();
         System.out.println("Using vertical snake");
      }
      else  {
         System.out.println("Using horizontal snake");
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
      int disCount = 0, j = 0, i = 0, startX = 0;
      ArrayList<Point> zone = new ArrayList<Point> ();
      String lastMove = "right";
     
      for (int ndx = 0; disList.size() < numDis; ndx++)   {
         startX = j;
         if (ndx % 2 == 0) {
            while (i < grid[0].length - 2)  {
               addCell(zone, new Point(j, i));
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
               addCell(zone, new Point(j, i));
                                 
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
               addCell(zone, new Point(j, i));
                                 
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
               addCell(zone, new Point(j, i));                
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
      int disCount = 0, j = 0, i = 0, startX;
      ArrayList<Point> zone = new ArrayList<Point> ();
     
      for (int ndx = 0; disList.size() < numDis; ndx++)   {
         startX = j;
         while ((i < grid[0].length - 2 && ndx % 2 == 0) || (i > 1 && ndx % 2 == 1))  {
            addCell(zone, new Point(j, i));
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
            addCell(zone, new Point(j, i));    
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
      int disCount = 0, j = 0, i = 0;
      ArrayList<Point> zone = new ArrayList<Point> ();
     
      for (int ndx = 0; disList.size() < numDis; ndx++)   {
         int startX = j;
         
         while ((i < grid[0].length && ndx % 2 == 0) || (i >= 0 && ndx % 2 == 1))  {
            if (disCount == 0)   {
               zone = new ArrayList<Point> ();
            }
            addCell(zone, new Point(j, i));
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
   
   private void addCell(ArrayList<Point> zone, Point p1)
   {
      /*if (grid[(int) p1.getX()][(int) p1.getY()] != 0)   {
         zone.add(p1);
      }*/
      zone.add(p1);
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
      
      return "" + (int) blu + "% blue " + (int) red + "% red\n"  + ratioB + "% blue representation, "
       + ratioR + "% red representation" + "\nPerimeter: " + totalPerim +
        "\nAverage Perimeter: " + totalPerim / numDis + "\nHighest Perimeter: " + highPerim;
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
   
   /**
      Interchange the x any y coordinates of the grid and the district zones.
   */
   private void reflectGrid()
   {
      double[][] newGrid = new double[grid[0].length][grid.length];
      
      for (int i = 0; i < grid[0].length; i++)  {
         for (int j = 0; j < grid.length; j++)  {
            newGrid[i][j] = grid[j][i];
         }
      }
      grid = newGrid;
      
      // Swap the district zones
      for (District dis: disList)   {
         dis.swapCoords(grid);
      }
            
   }
   
   private int calcPerim()
   {
      int temp = 0;
      
      for (District dis : disList)  {
         temp += dis.getPerim();
      }
      return temp;
   }
   
   public int calcHighPerim()
   {
      int highPerim = disList.get(0).getPerim();
      for (District d1 : disList)   {
         int newPerim = d1.getPerim();
         if (highPerim < newPerim)  {
            highPerim = newPerim;
         }
      }
      return highPerim;
   }   
   
   private boolean gridHasIsolation()
   {
      boolean ret = false;
      
      for (District dis : disList)  {
         if (dis.hasIsolation()) {
            ret = true;
         }
      }
      return ret;
   }
}  
