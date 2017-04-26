/** John Rice
   This class holds the fields and data for each district.
*/

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.Rectangle;
import java.awt.Color;

public class District
{
   private double[][] grid, popGrid;
   private double disPop = 0, blueRep = 0;
   private ArrayList<Point> zone;
   private String rep = "Tie";
   
   public District(double[][] grid, double[][] popGrid, ArrayList<Point> zone)
   {
      this.grid = grid;
      this.popGrid = grid;
      this.zone = zone;
      
      for (Point p1 : zone)   {
         disPop += popGrid[(int) p1.getX()][(int) p1.getY()];
      }
      for (int ndx = 0; ndx < zone.size(); ndx++)  {
         blueRep += popGrid[zone.get(ndx).x][zone.get(ndx).y] *
          grid[zone.get(ndx).x][zone.get(ndx).y];
      }
      blueRep /= disPop;
   }
   
   public void addSquare(Point spot)
   {
      zone.add(spot);
      
      blueRep *= disPop;
      disPop += popGrid[(int) spot.getX()][(int) spot.getY()];
      blueRep += popGrid[(int) spot.getX()][(int) spot.getY()] *
       grid[(int) spot.getX()][(int) spot.getY()];
      blueRep /= disPop;
   }
   
   public void removeSquare(Point spot)
   {
      zone.remove(spot);
      
      blueRep *= disPop;
      disPop -= popGrid[(int) spot.getX()][(int) spot.getY()];
      blueRep -= popGrid[(int) spot.getX()][(int) spot.getY()] *
       grid[(int) spot.getX()][(int) spot.getY()];
      blueRep /= disPop;
   }
   
   public double getPop()
   {
      return disPop;
   }
   
   public ArrayList<Point> getZone()
   {
      return zone;
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
      return blueRep;
   }
   
   /**
      Returns the proportion of squares in this district that are red.
   */
   public double getRedRep()
   {
      return 1 - blueRep;
   }
   
   /**
      Returns the proportion of squares in this district that would be blue
      if the district traded a blue square for a red square.
   */
   public double getBlueTradeRep(double voteChange)
   {
      double val = blueRep * disPop - voteChange;
      
      return val / disPop;
   }
   
   /**
      Returns the proportion of squares in this district that would be red
      if the district traded a red square for a blue square.
   */
   public double getRedTradeRep(double voteChange)
   {
      double val = getRedRep() * disPop + voteChange;
      
      return val / disPop;
   }
   
   public boolean inDistrict(Point spot)
   {
      return zone.contains(spot);
   }
   
   /**
      Returns the number of voters voting for the losing party in this district.
   */
   public double getNumLosingPop(int party)
   {
      if (party == 1 && getBlueRep() <= .5)  {
         return disPop * getBlueRep();
      }
      else if (party == 2 && getRedRep() <= .5) {
         return disPop * getRedRep();
      }
      return 0;
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
      if (getBlueRep() > .51)  {
         return 1;
      }
      if (getBlueRep() < .49)   {
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
	   if (zone.size() == 0)   {
	      return "Empty District";
	   }
	   String temp = "" + getRep() + " District: size " + disPop + " ";

	   temp = temp + String.format("(%d, %d) ", (int) zone.get(0).getX(), (int) zone.get(0).getY());
	   temp = temp + String.format("%.3f blue, %.3f red", getBlueRep(), getRedRep());
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
   
   /**
      Determine wether or not the district has an isolation, meaning
      there exists cells which are not connected to the main area of cells.
   */
   public boolean hasIsolation()
   {
      Set<Point> island = new HashSet<Point> ();
      if (zone.size() > 0) {
         checkNear(zone.get(0), island);
      }
      return island.size() != zone.size();
   }
   
   private void checkNear(Point thisPoint, Set<Point> island)
   {
      int x = (int) thisPoint.getX(), y = (int) thisPoint.getY();
      
      island.add(thisPoint);
      if (zone.contains(new Point(x + 1, y))
       && !island.contains(new Point(x + 1, y)))   {
         checkNear(new Point(x + 1, y), island);
      }
      if (zone.contains(new Point(x - 1, y))
       && !island.contains(new Point(x - 1, y)))   {
         checkNear(new Point(x - 1, y), island);
      }
      if (zone.contains(new Point(x, y + 1))
       && !island.contains(new Point(x, y + 1)))   {
         checkNear(new Point(x, y + 1), island);
      }
      if (zone.contains(new Point(x, y - 1))
       && !island.contains(new Point(x, y - 1)))   {
         checkNear(new Point(x, y - 1), island);
      }
   }
   
   /**
      Returns a double between 0 and 1 which represents the weight attached
      to this district when choosing a district to try to flip.
   */
   public double flipWeight(double numBluePop, int party)
   {
      double ret = 0;
      
      if (party == 1)   {
         ret = getBlueRep() * zone.size() / numBluePop;
      }
      else if (party == 2) {
         ret = getRedRep() * zone.size() / numBluePop;
      }
      return ret;
   }
   
   /**
      Returns a double between 0 and 1 which represents the weight attached
      to this district when choosing a district to trade with.
   */
   public double tradeWeight(int numNonComp, int numComp, int party, double voteChange)
   {
      double ret = 0, totalWeight = numNonComp + .5 * ((double) numComp);
      
      if ((party == 1 && Math.abs(getBlueTradeRep(voteChange) - .5) < .1)
       || (party == 2 && Math.abs(getRedTradeRep(voteChange) - .5) < .1))   {
         ret = .5 / totalWeight;
      }
      else  {
         ret = 1 / totalWeight;
      }
      return ret;
      
   }
   
   public boolean isCompetitive(int party, double voteChange)
   {
      boolean ret = false;
      
      if (party == 1)   {
         ret = Math.abs(getBlueTradeRep(voteChange) - .5) < .1;
      }
      else  {
         ret = Math.abs(getRedTradeRep(voteChange) - .5) < .1;
      }
      return ret;
   }
   
   /**
      Interchange the x and y values for each cell in the zone arrayList.
   */
   public void swapCoords(double[][] newGrid)
   {
      grid = newGrid;
      
      ArrayList<Point> temp = new ArrayList<Point> ();
      
      for (Point p1 : zone)   {
         temp.add(new Point((int) p1.getY(), (int) p1.getX()));
      }
      zone = temp;
   }
}
