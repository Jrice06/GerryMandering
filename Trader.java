/* John Rice
   This class makes several trades between districts to try to get to a "fair"
   representation.
*/

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.Math;
import java.util.Random;

public class Trader
{
   private static final int NUM_ITER = 250, ITER_MAX = 500;
   private static final double POP_THRESH = .05;
   private static final double TRADE_MAX = 5;
   private double[][] grid, popGrid;
   private ArrayList<District> disList;
   private int totalPerim, highPerim;
   private double repRatio, popRatio, disPop;
   private TradeProp lastTrade = null;
   private Random rand = new Random ();
   
   public Trader(double[][] grid, double[][] popGrid, ArrayList<District> disList,
    double disPop, double popRatio)
   {
      this.grid = grid;
      this.popGrid = popGrid;
      this.disList = disList;
      this.disPop = disPop;
      this.popRatio = popRatio;
      
      totalPerim = calcPerim(disList);
   }
   
   /**
      This class contains all the information for a potential trade
      proposition.
   */
   private class TradeProp
   {
      int perimChange;
      double voteChange = 0;
      ArrayList<Point> ourCells, theirCells;
      District ourDis, theirDis;
      
      public TradeProp(ArrayList<Point> ourCells, ArrayList<Point> theirCells,
       District ourDis, District theirDis)
      {
         this.ourCells = ourCells;
         this.theirCells = theirCells;
         this.ourDis = ourDis;
         this.theirDis = theirDis;
         
         for (Point our : ourCells) {
            this.voteChange += grid[(int) our.getX()][(int) our.getY()] *
             popGrid[(int) our.getX()][(int) our.getY()];
         }
         for (Point their : theirCells)   {
            this.voteChange -= grid[(int) their.getX()][(int) their.getY()] *
             popGrid[(int) their.getX()][(int) their.getY()];
         }
      }
      
      public String toString()
      {
         String temp = "";
         
         for (Point cell : ourCells)   {
            temp = temp + cell.toString() + ", ";
         }
         temp = temp + "for ";
         for (Point cell : theirCells) {
            temp = temp + cell.toString() + ", ";
         }
         return temp + "Perim Change: " + perimChange + " Vote Change: " +
          String.format("%.3f", voteChange);
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
   
   public int calcHighPerim(ArrayList<District> disList)
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
   
   /**
      Runs a pre-determined number of simulations for solving the grid,
      and then picks the result which has the lowest overall perimeter.
   */
   public void manySolve()
   {
      ArrayList<District> original = disList, bestList = disList;
      int bestPerim = Integer.MAX_VALUE / 3, bestHighPerim = Integer.MAX_VALUE / 3;
      int count = 0;
      double targetRatio, party;
      
      ArrayList<Double> val = getTargetRatio();
      party = val.get(0);
      targetRatio = val.get(1);
      
      System.out.println("Target ratio: " + ((int) (targetRatio * 100)) + "%");
      System.out.println("Tolerating " + NUM_ITER + " simulations of failure");
      long start = System.currentTimeMillis();
      for (int ndx = 0; ndx < NUM_ITER; ndx++)  {
         System.out.print(ndx + " ");
         ArrayList<District> temp = new ArrayList<District> ();
         copyDisList(original, temp);
         disList = temp;
         
         setRatio();
         boolean solved = randomSolve(targetRatio, (int) party);
         int perim = calcPerim(disList), highPerim = calcHighPerim(disList);
         
         if (perim + highPerim < bestPerim + bestHighPerim && solved) {
            System.out.println();
            System.out.println("New Best! Attempt number: " + count + " | Total perim: " + perim
             + " | High Perim: " + highPerim);
            bestPerim = perim;
            bestHighPerim = highPerim;
            bestList = temp;
            ndx = 0;
         }
         setRatio();
         count++;
      }
      copyDisList(bestList, original);
      setRatio();
      long end = System.currentTimeMillis();
      System.out.println("Execution Time: " + (((float)(end - start)) / 1000) + " seconds");
   }
   
   /**
      Returns an arraylist of two elements, the first is the party to support
      and the second is the target ratio.
   */
   public ArrayList<Double> getTargetRatio()
   {
      double target = 0, close = popRatio, targetRatio, party = 1;
      double targetAdd = .5;
      ArrayList<Double> ret = new ArrayList<Double> ();
      
      // Find the target ratio of representation to achieve.
      for (int ndx = 0; ndx < disList.size() * 2; ndx++)  {
         if (Math.abs((target + targetAdd) / disList.size() - popRatio) < close)   {
            target += targetAdd;
            close = Math.abs(target / disList.size() - popRatio);
         }
         else  {
            break;
         }
      }
      targetRatio = target / disList.size();
      
      // Figure out which party to support.
      if (targetRatio < repRatio)   {
         party = 2;
      }
      ret.add(party);
      ret.add(targetRatio);
      return ret;
   }
   
   /**
      Makes one trade according to the deterministic algorithm.
   */
   public void makeBestTrade()
   {
      int party;
      double targetRatio, close;
      
      ArrayList<Double> ret = getTargetRatio();
      party = (int) ((double) ret.get(0));
      targetRatio = ret.get(1);
      System.out.println("TargetRatio: " + (int) (targetRatio * 100) + "% blue");
     
      if (repRatio != targetRatio)  {
         close = .5;
         District bestDis = disList.get(0);
         
         // Choose the district to make a trade in.
         if (party == 1)   {
            for (District dis : disList)  {
               if (dis.getBlueRep() <= .51 && Math.abs(.5 - dis.getBlueRep()) < close) {
                  close = Math.abs(.5 - dis.getBlueRep());
                  bestDis = dis;
               }
            }
         }
         else  {
            for (District dis : disList)  {
               if (dis.getRedRep() <= .51 && Math.abs(.5 - dis.getRedRep()) < close) {
                  close = Math.abs(.5 - dis.getRedRep());
                  bestDis = dis;
               }
            }
         }
         
         // Now that we know the district and the party to support, we can make a trade.
         evaluateTrades(bestDis, party);
       }
       else    {
         
         // If the target ratio has been achieved, look for a cleanup trade instead.
         for (District dis : disList)  {
            for (District border : dis.getBorderDistricts(disList))   {
               if (cleanUpTrade(dis, border, false))  {
	               return;
	            }
	         }
	      }
	   }
   }
   
   /**
      Makes a somewhat random sequence of trades to completely solve a grid.
      Returns true if no districts were abandonded mid solve, false otherwise.
      A district is abandonded if it exceeds the iteration maximum or it cannot make
      a single trade.
   */
   public boolean randomSolve(double targetRatio, int party)
   {
      boolean noAbandon = true;
      
      while (repRatio != targetRatio && noAbandon)  {
         noAbandon = flipRandomDistrict(party);
         setRatio();
      }
      
      // Once the ratios are matched, perform any trades which reduce overall perimeter
      // and don't change the ratios of representation.
      for (District dis : disList)  {
         for (District border : dis.getBorderDistricts(disList))   {
            while (cleanUpTrade(dis, border, false))  {
	            ;
	         }
	      }
	   }
      return noAbandon;   
   }
   
   /**
      This method chooses a random district that the under-represented party is losing and flips it.
      Returns true if the method was able to flip the district, false otherwise.
   */
   public boolean flipRandomDistrict(int party)
   {
      double numBluePop = 0;
      ArrayList<District> partyLosing = new ArrayList<District> ();
      for (District dis : disList)  {
         if ((party == 1 && dis.getBlueRep() <= .51) ||
          (party == 2 && dis.getRedRep() <= .51)) {
            partyLosing.add(dis);
            numBluePop += dis.getNumLosingPop(party);
         }
      }
      
      double randNum = rand.nextDouble();
      int ndx = -1;
      
      // Pick the district to flip
      while (randNum > 0 ) {
         ndx++;
         randNum -= partyLosing.get(ndx).flipWeight(numBluePop, party);
      }
      District toFlip = partyLosing.get(ndx);
      
      return flipDistrict(party, toFlip);
   }
   
   /**
      This method flips the supplied district in favor of the under-represented party.
      Returns true if the district was able to be flipped, false otherwise.
   */
   public boolean flipDistrict(int party, District dis)
   {
      boolean madeTrade = true;
      int numIter = 0;
      
      while (((party == 1 && dis.getBlueRep() <= .51) ||
       (party == 2 && dis.getRedRep() <= .51)) && madeTrade && numIter < ITER_MAX)   {
         madeTrade = evaluateRandomTrade(dis, party);
         numIter++;
      }
      return madeTrade && numIter < ITER_MAX;
   }
   
   /**
      Evaluates all possible trades for the specified district for
      the benefit of the specified party. 
      "party" is 1 for the Blue party and 2 for the Red party.
      Returns true if a trade is made, false otherwise.
   */ 
   private boolean evaluateTrades(District temp, int party)
   {
      ArrayList<District> borderDistricts = temp.getBorderDistricts(disList);
      ArrayList<TradeProp> tradeList = new ArrayList<TradeProp> ();
      for (District dis : borderDistricts)  {
         tradeList.add(pickBestTrade(temp, dis, party));
      }
      
      double value = -100, tempVal;
      TradeProp bestTrade = tradeList.get(0);
      for (TradeProp trade : tradeList)   {        
         if (trade != null && party == 1)   {
            tempVal = tradeValue(Math.abs(trade.theirDis.getBlueTradeRep(trade.voteChange) - .5),
             trade);
            if (tempVal > value) {
               bestTrade = trade;
               value = tempVal;
            }
         }
         else if (trade != null && party == 2)  {
            tempVal = tradeValue(Math.abs(trade.theirDis.getRedTradeRep(trade.voteChange) - .5),
             trade);
            if (tempVal > value) {
               bestTrade = trade;
               value = tempVal;
            }
         }
      }
      
      if (bestTrade != null)  {
         System.out.println("Best Trade: " + bestTrade);
         makeTrade(bestTrade);
         lastTrade = bestTrade;
         return true;
      }    
      return false;
   }
   
   /**
      Functions the same way as the evaluate trades method except that it
      choses a random district to trade with.
      Returns true if a trade was made, false otherwise.
   */
   private boolean evaluateRandomTrade(District temp, int party)
   {
      int numComp = 0;
      ArrayList<District> borderDistricts = temp.getBorderDistricts(disList);
      ArrayList<TradeProp> tradeList = new ArrayList<TradeProp> ();
      ArrayList<District> validDistricts = new ArrayList<District> ();
      
      for (District dis : borderDistricts)  {
         TradeProp prop = pickBestTrade(temp, dis, party);
         if (prop != null) {
            tradeList.add(prop);
            validDistricts.add(dis);
            if (dis.isCompetitive(party, prop.voteChange)) {
               numComp++;
            }
         }
      }
      
      if (tradeList.size() == 0) {
         return false;
      }
      double randNum = rand.nextDouble();
      int ndx = -1;
         
      while (randNum > 0 && ndx < validDistricts.size())  {
         ndx++;
         randNum -=
          validDistricts.get(ndx).tradeWeight(validDistricts.size() - numComp,
           numComp, party, tradeList.get(ndx).voteChange);
      }
      TradeProp randomTrade = tradeList.get(ndx);
      //TradeProp randomTrade = tradeList.get(rand.nextInt(tradeList.size()));
      
      makeTrade(randomTrade);
      lastTrade = randomTrade;
      return true;   
   }
            
   
   /**
      For a specified district, picks the best possible trade between ourDis and theirDis.
      Best possible means lowest perimeter change.
      Returns the proposed trade, or null if no trade is possible.
   */
   private TradeProp pickBestTrade(District ourDis, District theirDis, int party)
   {
      TradeProp bestTrade = null;
         
      double tradeValue = Double.MAX_VALUE;
      int curPerim = calcPerim(disList), futPerim;
      
      ArrayList<TradeProp> tradeList = getValidTrades(ourDis, theirDis);
      
      for (TradeProp trade : tradeList)   {
         double voteChange = trade.voteChange;
         
         if (party == 1)   {
            voteChange *= -1;
         }
         if (voteChange > 0)  {
            makeTrade(trade);
            if (!ourDis.hasIsolation() && !theirDis.hasIsolation())  {
               futPerim = calcPerim(disList);
               trade.perimChange = futPerim - curPerim;
               if (trade.perimChange / voteChange < tradeValue)   {
                  tradeValue = trade.perimChange / voteChange;
                  bestTrade = trade;
               }
            }
            makeTrade(trade);
         }
      }
      return bestTrade;
   }
   
   /*
      Makes the specified Trade.  Order does not matter.
   */
   public void makeTrade(TradeProp trade)
   {
      if (trade.ourDis.contains(trade.ourCells.get(0)))   { 
         for (Point ourCell : trade.ourCells)   {
            trade.ourDis.removeSquare(ourCell);
            trade.theirDis.addSquare(ourCell);
         }
         for (Point theirCell : trade.theirCells)  {
            trade.theirDis.removeSquare(theirCell);
            trade.ourDis.addSquare(theirCell);
         }
      }
      
      else if (trade.ourDis.contains(trade.theirCells.get(0)))  {         
         for (Point ourCell : trade.ourCells)   {
            trade.theirDis.removeSquare(ourCell);
            trade.ourDis.addSquare(ourCell);
         }
         for (Point theirCell : trade.theirCells)  {
            trade.ourDis.removeSquare(theirCell);
            trade.theirDis.addSquare(theirCell);
         }
      }
   }
   
   /**
      If party is false, looks for trades that have no impact on representation but result in negative
      perimeter changes.  If party is true, then any trade can be made.
      Returns true if a clean up trade was made, false otherwise. 
  */
   public boolean cleanUpTrade(District ourDis, District theirDis, boolean party)
   {
      TradeProp bestTrade = null;
      ArrayList<Point> theirBorder = ourDis.getBorderCells(theirDis);
      ArrayList<Point> ourBorder = theirDis.getBorderCells(ourDis);
      int perimChange = 0, curPerim = calcPerim(disList), futPerim;
      
      ArrayList<TradeProp> tradeList = getValidTrades(ourDis, theirDis);
      for (TradeProp trade : tradeList)   {
         if (trade.ourCells.size() + trade.theirCells.size() > 4) {
            //System.out.println(trade);
         }
         if (trade.ourCells.size() > 1 && trade.theirCells.size() > 1)  {
            //System.out.println(trade);
         }
         double oldRatio = repRatio;
         makeTrade(trade);
         setRatio();
         
         if (!ourDis.hasIsolation() && !theirDis.hasIsolation() &&
          (repRatio == oldRatio || party)) {
            futPerim = calcPerim(disList);
            trade.perimChange = futPerim - curPerim;
            if (trade.perimChange < perimChange) {
               perimChange = futPerim - curPerim;
               bestTrade = trade;
            }
         }
         makeTrade(trade);
         setRatio();
      }
      if (bestTrade != null)  {
         makeTrade(bestTrade);
         lastTrade = bestTrade;
         return true;
      }
      return false;
   }
   
   public void updateRepRatio(double repRatio)
   {
      this.repRatio = repRatio;
   }
   
   // Undos the last trade to be made.
   public void undoTrade()
   {
      if (lastTrade != null)  {
         makeTrade(lastTrade);
      }
   }
   
   /**
      Returns a double which indicates how valuable a trade is in the global context.
      Higher value means a better trade.
   */
   private double tradeValue(double repToFlip, TradeProp trade)
   {
      double retVal = 0;
      
      if (repToFlip >= .02) {
         retVal = Double.MAX_VALUE / 2;
      }
      else if (repToFlip >= .01)  {
         retVal = Double.MAX_VALUE / 4;
      }
      retVal -= trade.perimChange / trade.voteChange;
      return retVal;
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
   
   /**
      Makes a copy of a list of districts and overrides the copyList argument.
   */
   public void copyDisList(ArrayList<District> listToCopy, ArrayList<District> copyList)
   {
      copyList.clear();
      
      for (District dis : listToCopy)  {
         ArrayList<Point> newZone = new ArrayList<Point> ();
         for (Point p1 : dis.getZone())   {
            newZone.add(p1);
         }
         District newDis = new District(grid, popGrid, newZone);
         copyList.add(newDis);
      }
   }
   
   /**
      Returns a list of 1 for 1 OR 2 for 1 OR 1 for 2 trades between two
      districts all of which do not violate the equal population requirements
      for both districts.
   */
   public ArrayList<TradeProp> getValidTrades(District ourDis, District theirDis)
   {
      ArrayList<TradeProp> tradeList = new ArrayList<TradeProp> ();
      ArrayList<Point> theirBorder = ourDis.getBorderCells(theirDis);
      ArrayList<Point> ourBorder = theirDis.getBorderCells(ourDis);
      
      for (Point theirCell : theirBorder) {
         for (Point ourCell : ourBorder)  {
            /*TradeProp temp = getTradeProp(ourDis, theirDis, ourCell, theirCell);
            if (temp != null) {
               tradeList.add(temp);
            }*/
            
            
            double ourPop = ourDis.getPop() - popGrid[(int) ourCell.getX()][(int) ourCell.getY()]
             + popGrid[(int) theirCell.getX()][(int) theirCell.getY()];
            double theirPop = theirDis.getPop() - popGrid[(int) theirCell.getX()][(int) theirCell.getY()]
             + popGrid[(int) ourCell.getX()][(int) ourCell.getY()];
             
            if ((ourPop - disPop) / disPop > POP_THRESH ||
             (theirPop - disPop) / disPop < -POP_THRESH) {
               for (Point ourCellTwo : ourBorder)  {
                  if (!ourCellTwo.equals(ourCell)) {
                     double ourNewPop = ourPop -
                      popGrid[(int) ourCellTwo.getX()][(int) ourCellTwo.getY()];
                     double theirNewPop = theirPop +
                      popGrid[(int) ourCellTwo.getX()][(int) ourCellTwo.getY()];
                      
                     if (Math.abs(ourNewPop - disPop) / disPop < POP_THRESH &&
                      Math.abs(theirNewPop - disPop) / disPop < POP_THRESH)   {
                        ArrayList<Point> ourTradeCells = new ArrayList<Point> ();
                        ArrayList<Point> theirTradeCells = new ArrayList<Point> ();
                        
                        ourTradeCells.add(ourCell);
                        ourTradeCells.add(ourCellTwo);
                        theirTradeCells.add(theirCell);
                        
                        tradeList.add(new TradeProp(ourTradeCells, theirTradeCells,
                         ourDis, theirDis));
                     }
                  }
               }
            }
            else if ((ourPop - disPop) / disPop < -POP_THRESH ||
             (theirPop - disPop) / disPop > POP_THRESH) {
               for (Point theirCellTwo : theirBorder)  {
                  if (!theirCellTwo.equals(theirCell)) {
                  
                     double theirNewPop = theirPop -
                      popGrid[(int) theirCellTwo.getX()][(int) theirCellTwo.getY()];
                     double ourNewPop = ourPop +
                      popGrid[(int) theirCellTwo.getX()][(int) theirCellTwo.getY()];
                      
                     if (Math.abs(ourNewPop - disPop) / disPop < POP_THRESH &&
                      Math.abs(theirNewPop - disPop) / disPop < POP_THRESH)   {
                        ArrayList<Point> ourTradeCells = new ArrayList<Point> ();
                        ArrayList<Point> theirTradeCells = new ArrayList<Point> ();
                        
                        ourTradeCells.add(ourCell);
                        theirTradeCells.add(theirCell);
                        theirTradeCells.add(theirCellTwo);
                        
                        tradeList.add(new TradeProp(ourTradeCells, theirTradeCells,
                         ourDis, theirDis));
                     }
                  }
               }
            }
            else  {
               ArrayList<Point> ourTradeCells = new ArrayList<Point> ();
               ArrayList<Point> theirTradeCells = new ArrayList<Point> ();
               
               ourTradeCells.add(ourCell);
               theirTradeCells.add(theirCell);
               tradeList.add(new TradeProp(ourTradeCells, theirTradeCells,
                ourDis, theirDis));
            }
         }
      }
      return tradeList;
   }
   
   /**
      Returns a single trade proposition for the given starter cells which satisfies the
      population requirements, or null if no such trade exists.  This proposition has a max number
      of cells that can be involved in the trade.
   */
   private TradeProp getTradeProp(District ourDis, District theirDis, Point ourCell, Point theirCell)
   {      
      ArrayList<Point> ourTradeCells = new ArrayList<Point> (),
       theirTradeCells = new ArrayList<Point> ();
      ourTradeCells.add(ourCell);
      theirTradeCells.add(theirCell);
      int count = 0;
      double ourPopAfterTrade = getOurPopAfterTrade(ourTradeCells, theirTradeCells,
       ourDis);
      double theirPopAfterTrade = getTheirPopAfterTrade(ourTradeCells, theirTradeCells,
       theirDis);
      
      while ((Math.abs(ourPopAfterTrade - disPop) > POP_THRESH * disPop ||
       Math.abs(theirPopAfterTrade - disPop) > POP_THRESH * disPop) && count++ < TRADE_MAX)   {
          
         if (ourPopAfterTrade - disPop > POP_THRESH * disPop ||
          theirPopAfterTrade - disPop < -POP_THRESH * disPop) {
          
            Point bestPoint = getBestPoint(ourDis, theirDis, ourTradeCells, theirTradeCells);
            ourTradeCells.add(bestPoint);
         }
         else if (theirPopAfterTrade - disPop > POP_THRESH * disPop ||
          ourPopAfterTrade - disPop < -POP_THRESH * disPop)  { 
                    
            Point bestPoint = getBestPoint(theirDis, ourDis, theirTradeCells, ourTradeCells);
            theirTradeCells.add(bestPoint);
         }
         ourPopAfterTrade = getOurPopAfterTrade(ourTradeCells, theirTradeCells,
          ourDis);
         theirPopAfterTrade = getTheirPopAfterTrade(ourTradeCells, theirTradeCells,
          theirDis);
      }
      
      boolean failed = false;
      if (Math.abs(ourPopAfterTrade - disPop) > POP_THRESH * disPop ||
       Math.abs(theirPopAfterTrade - disPop) > POP_THRESH * disPop)   {
         failed = true;
      }
            
      if (!failed)   {
         TradeProp finalTrade = new TradeProp(ourTradeCells, theirTradeCells, ourDis, theirDis);
         return finalTrade;
      }
      return null;
   }
   
   /**
      Returns the point in our district which, if traded, would result in populations
      that are closested to the target district population for both districts involved.
      Orig is the district that we originally wanted to increase the vote for.
   */
   private Point getBestPoint(District ourDis, District theirDis,
    ArrayList<Point> ourTradeCells, ArrayList<Point> theirTradeCells)
   {
      double smallError = Double.MAX_VALUE;
      ArrayList<Point> ourBorder = theirDis.getBorderCells(ourDis);
      Point bestPoint = ourBorder.get(0);
         
      for (Point p1 : ourBorder) {
         if (!ourTradeCells.contains(p1)) {
            ourTradeCells.add(p1);
            double ourPopAfterTrade = getOurPopAfterTrade(ourTradeCells, theirTradeCells,
             ourDis);
            double theirPopAfterTrade = getTheirPopAfterTrade(ourTradeCells, theirTradeCells,
             theirDis);
             
            double error = Math.max(Math.abs(ourPopAfterTrade - disPop),
             Math.abs(theirPopAfterTrade - disPop));
            
            if (error < smallError)  {
               smallError = error;
               bestPoint = p1;
            }
            ourTradeCells.remove(p1);
         }
      }
      return bestPoint;
   }
   
   private double getOurPopAfterTrade(ArrayList<Point> ourTradeCells,
    ArrayList<Point> theirTradeCells, District ourDis)  {
    
      double ourNewPop = ourDis.getPop();
      for (Point p1 : ourTradeCells)   {
         ourNewPop -= popGrid[(int) p1.getX()][(int) p1.getY()];
      }
      for (Point p1 : theirTradeCells) {
         ourNewPop += popGrid[(int) p1.getX()][(int) p1.getY()];
      }
      return ourNewPop;
   }
   
   private double getTheirPopAfterTrade(ArrayList<Point> ourTradeCells,
    ArrayList<Point> theirTradeCells, District theirDis)  {
    
      double theirNewPop = theirDis.getPop();
      for (Point p1 : theirTradeCells)   {
         theirNewPop -= popGrid[(int) p1.getX()][(int) p1.getY()];
      }
      for (Point p1 : ourTradeCells) {
         theirNewPop += popGrid[(int) p1.getX()][(int) p1.getY()];
      }
      return theirNewPop;
   }
}
