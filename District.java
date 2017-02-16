/** John Rice
   This class holds the fields and data for each district.
*/

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.Rectangle;
import java.awt.Color;

public class District
{
   private int[][] grid;
   private ArrayList<Point> zone;
   private String rep = "Tie";
   
   public District(int[][] grid, ArrayList<Point> zone)
   {
      this.grid = grid;
      this.zone = zone;
   }
   
   public void addSquare(Point spot)
   {
      zone.add(spot);
   }
   
   public void removeSquare(Point spot)
   {
      zone.remove(spot);
   }
   
   public int getSize()
   {
      return zone.size();
   }
   
   public boolean contains(Point temp)
   {
      return zone.contains(temp);
   }
   
   /**
      Returns the proportion of squares in this district that are blue.
   */
   public double getBlueRep()
   {
      double blue = 0;
      
      for (int ndx = 0; ndx < zone.size(); ndx++)  {
         if (grid[zone.get(ndx).x][zone.get(ndx).y] == 1) {
            blue++;
         }
      }
      return blue / zone.size();
   }
   
   /**
      Returns the proportion of squares in this district that are red.
   */
   public double getRedRep()
   {
      return 1 - getBlueRep();
   }
   
   /**
      Returns the proportion of squares in this district that would be blue
      if the district traded a blue square for a red square.
   */
   public double getBlueTradeRep()
   {
      double temp = getBlueRep();
      double val = temp * zone.size() - 1;
      return val / zone.size();
   }
   
   /**
      Returns the proportion of squares in this district that would be red
      if the district traded a red square for a blue square.
   */
   public double getRedTradeRep()
   {
      double temp = getRedRep();
      double val = temp * zone.size() - 1;
      
      return val / zone.size();
   }
   
   public boolean inDistrict(Point spot)
   {
      return zone.contains(spot);
   }
   
   public int drawDis(Graphics2D g2, int rexSize)
   {
      int totalPerim = 0;
      for (Point p1 : zone)   {
         totalPerim += drawSquare(g2, rexSize, p1, (int) p1.getX(), (int) p1.getY());
      }
      return totalPerim;
   }
   
   public int isBlue()
   {
      if (getBlueRep() > .5)  {
         return 1;
      }
      if (getBlueRep() < .5)   {
         return 2;
      }
      return 0;
   }
   
   public String getRep()
   {
      if (isBlue() == 1)  {
	      rep = "Blue";
	   }
	   else if (isBlue() == 2)  {
	      rep = "Red";
	   }
	   else  {
	      rep = "Tie";
	   }
      return rep;
   }
   
   
   /*
      Handles the drawing of the boundaries of the square, depending on wether this
      square borders squares of a different district.
      Returns the number of squares that border this square but belong to a different district.
   */
   public int drawSquare(Graphics2D g2, int rexSize, Point spot, int ndxA, int ndxB)
   {
      int x = (int) spot.getX(), y = (int) spot.getY();
      int perim = 0;
      
		g2.setColor(Color.black);
		
		if (!inDistrict(new Point(x - 1, y)))  {
		   perim++;
		   g2.fill(new Rectangle.Double(rexSize * (ndxA + .95), rexSize * (ndxB + 1), rexSize * .1, rexSize));
		}
      if (!inDistrict(new Point(x, y - 1)))  {
         perim++;
		   g2.fill(new Rectangle.Double(rexSize * (ndxA + 1), rexSize * (ndxB + .95), rexSize, rexSize * .1));
		}
		if (!inDistrict(new Point(x + 1, y)))  {
		   perim++;
		   g2.fill(new Rectangle.Double(rexSize * (ndxA + 1.95), rexSize * (ndxB + 1), rexSize * .1, rexSize));
		}
		if (!inDistrict(new Point(x, y + 1)))  {
		   perim++;
		   g2.fill(new Rectangle.Double(rexSize * (ndxA + 1), rexSize * (ndxB + 1.95), rexSize, rexSize * .1));
		}
		return perim;
	}
	
	public String toString()
	{
	   String temp = "" + getRep() + " District:, size " + zone.size() + " ";

	   for (Point p1 : zone)   {
	      temp = temp + String.format("(%d, %d) ", (int) p1.getX(), (int) p1.getY());
	   }
	   return temp;
   }
   
   public int getPerim()
   {
      int totalPerim = 0;
      
      for (Point cell : zone) {
         int x = (int) cell.getX(), y = (int) cell.getY();
         
         if (!inDistrict(new Point(x - 1, y)))  {
            totalPerim++;
         }
         if (!inDistrict(new Point(x + 1, y)))  {
            totalPerim++;
         }
         if (!inDistrict(new Point(x, y - 1)))  {
            totalPerim++;
         }
         if (!inDistrict(new Point(x, y + 1)))  {
            totalPerim++;
         }
      }
      return totalPerim;
   }
   
   public void tradeCell(Point ourGuy, Point theirGuy, District them)
   {
      if (!zone.contains(ourGuy) || !them.zone.contains(theirGuy))   {
         return;
      }
      
      this.zone.add(theirGuy);
      this.zone.remove(ourGuy);
      them.zone.add(ourGuy);
   }
   
   // Returns a list of points from their distrct that border this distect and their district
   public ArrayList<Point> getBorderCells(District them)
   {
      ArrayList<Point> border = new ArrayList<Point> ();
      for (Point theirCell : them.zone)  {
         for (Point ourCell : this.zone) {
            if (Math.abs(ourCell.getX() - theirCell.getX()) == 1
             && ourCell.getY() == theirCell.getY()) {
               border.add(theirCell);
               break;
            }
            else if (ourCell.getX() == theirCell.getX()
             && Math.abs(ourCell.getY() - theirCell.getY()) == 1) {
               border.add(theirCell);
               break;
            }
         }
      }
      return border;
   }
   
   // Returns a list of districts that border this district.
   public ArrayList<District> getBorderDistricts(ArrayList<District> disList)
   {
      ArrayList<District> borders = new ArrayList<District> ();
      for (Point cell : zone) {
         int x = (int) cell.getX(), y = (int) cell.getY();
         
         if (!inDistrict(new Point(x - 1, y)))  {
            District temp = getDistrictContain(new Point(x - 1, y), disList);
            if (!borders.contains(temp) && temp != null)   {
               borders.add(temp);
            }
         }
         if (!inDistrict(new Point(x + 1, y)))  {
            District temp = getDistrictContain(new Point(x + 1, y), disList);
            if (!borders.contains(temp) && temp != null)   {
               borders.add(temp);
            }
         }
         if (!inDistrict(new Point(x, y - 1)))  {
            District temp = getDistrictContain(new Point(x, y - 1), disList);
            if (!borders.contains(temp) && temp != null)   {
               borders.add(temp);
            }
         }
         if (!inDistrict(new Point(x, y + 1)))  {
            District temp = getDistrictContain(new Point(x, y + 1), disList);
            if (!borders.contains(temp) && temp != null)   {
               borders.add(temp);
            }
         }
      }
      return borders;
   }
   
   // Returns the district from the district list that contains the specified cell.
   public District getDistrictContain(Point cell, ArrayList<District> disList)
   {
      for (District dis : disList)   {
         if (dis.zone.contains(cell)) {
            return dis;
         }
      }
      return null;
   }
}
