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
   private static final int NUM_ITER = 500;
   private int[][] grid;
   private ArrayList<District> disList;
   private int disPop, totalPerim, highPerim;
   private double repRatio, popRatio;
   private TradeProp lastTrade = null;
   private Random rand = new Random ();
   
   public Trader(int[][] grid, ArrayList<District> disList, int disPop, double popRatio)
   {
      this.grid = grid;
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
      Point ourCell, theirCell;
      District ourDis, theirDis;
      
      public TradeProp(Point ourCell, Point theirCell, District ourDis, District theirDis)
      {
         this.ourCell = ourCell;
         this.theirCell = theirCell;
         this.ourDis = ourDis;
         this.theirDis = theirDis;
      }
      
      public String toString()
      {
         return ourCell.toString() + " for " + theirCell.toString() +
          " Perim Change: " + perimChange;
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
   
   /**
      Runs a pre-determined number of simulations for solving the grid,
      and then picks the result which has the lowest overall perimeter.
   */
   public void manySolve()
   {
      ArrayList<District> original = disList, bestList = disList;
      int bestPerim = Integer.MAX_VALUE;
    
      System.out.println("Starting " + NUM_ITER + " simulations");
      long start = System.currentTimeMillis();
      for (int ndx = 0; ndx < NUM_ITER; ndx++)  {
         ArrayList<District> temp = new ArrayList<District> ();
         copyDisList(original, temp);
         disList = temp;
         
         setRatio();
         randomSolve();
         int perim = calcPerim(disList);
         //System.out.println("Attempt number: " + ndx + " | Total perim: " + perim);
         if (perim < bestPerim) {
            //System.out.println("New Best");
            bestPerim = perim;
            bestList = temp;
         }
         setRatio();
      }
      copyDisList(bestList, original);
      setRatio();
      long end = System.currentTimeMillis();
      System.out.println("Execution Time: " + (((float)(end - start)) / 1000) + " seconds");
   }
   
   /**
      Makes one trade according to the deterministic algorithm.
   */
   public void makeBestTrade()
   {
      int party = 1;
      double target = 0, close = popRatio, targetRatio;
      double targetAdd = .5;
      
      if (lastTrade != null)  {
         boolean needClean = cleanUpTrade();
         if (needClean) {
            return;
         }
      }
      
      if (disPop % 2 == 1) {
         targetAdd = 1;
      }
      
      // Find the target ratio of representation to achieve.
      for (int ndx = 0; ndx < disList.size() * 2; ndx++)  {
         if (Math.abs((target + .5) / disList.size() - popRatio) < close)   {
            target += targetAdd;
            close = Math.abs(target / disList.size() - popRatio);
         }
         else  {
            ndx = disList.size() * 2;
         }
      }
      System.out.println("---------------------------------------------------------------");
      System.out.println((int) (popRatio * 100) + "% blue, " +
       (int) ((1 - popRatio) * 100) + "% red");
      targetRatio = target / disList.size();
      
      System.out.println("TargetRatio: " + (int) (targetRatio * 100) + "% blue");
      // Figure out which party to support.
      if (targetRatio < repRatio)   {
         party = 2;
      }
      
      if (repRatio != targetRatio)  {
         close = .5;
         District bestDis = disList.get(0);
         
         // Choose the district to make a trade in.
         if (party == 1)   {
            for (District dis : disList)  {
               if (dis.getBlueRep() <= .5 && .5 - dis.getBlueRep() < close) {
                  close = .5 - dis.getBlueRep();
                  bestDis = dis;
               }
            }
         }
         else  {
            for (District dis : disList)  {
               if (dis.getRedRep() <= .5 && .5 - dis.getRedRep() < close) {
                  close = .5 - dis.getRedRep();
                  bestDis = dis;
               }
            }
         }
         
         // Now that we know the district and the party to support, we can make a trade.
         evaluateTrades(bestDis, party);
       }
   }
   
   /**
      Makes a somewhat random sequence of trades to completely solve a grid.
   */
   public void randomSolve()
   {
      int party = 1;
      double target = 0, close = popRatio, targetRatio;
      double targetAdd = .5;
      
      if (disPop % 2 == 1) {
         targetAdd = 1;
      }
      
      // Find the target ratio of representation to achieve.
      for (int ndx = 0; ndx < disList.size() * 2; ndx++)  {
         if (Math.abs((target + .5) / disList.size() - popRatio) < close)   {
            target += targetAdd;
            close = Math.abs(target / disList.size() - popRatio);
         }
         else  {
            ndx = disList.size() * 2;
         }
      }
      targetRatio = target / disList.size();
      
      // Figure out which party to support.
      if (targetRatio < repRatio)   {
         party = 2;
      }
      
      while (repRatio != targetRatio)  {
         flipRandomDistrict(party);
         setRatio();
      }      
   }
   
   /**
      This method chooses a random district that the under-represented party is losing and flips it.
   */
   public void flipRandomDistrict(int party)
   {  
      // Pick the district to flip
      ArrayList<District> partyLosing = new ArrayList<District> ();
      for (District dis : disList)  {
         if (party == 1 && dis.getBlueRep() <= .5) {
            partyLosing.add(dis);
         }
         else if (party == 2 && dis.getRedRep() <= .5)   {
            partyLosing.add(dis);
         }
      }
      District toFlip = partyLosing.get(rand.nextInt(partyLosing.size()));
      flipDistrict(party, toFlip);
   }
   
   /**
      This method chooses a random district that the under-represented party is losing and flips it.
   */
   public void flipRandomDistrict()
   {
      int party = 1;
      double target = 0, close = popRatio, targetRatio;
      double targetAdd = .5;
      
      if (disPop % 2 == 1) {
         targetAdd = 1;
      }
      
      // Find the target ratio of representation to achieve.
      for (int ndx = 0; ndx < disList.size() * 2; ndx++)  {
         if (Math.abs((target + .5) / disList.size() - popRatio) < close)   {
            target += targetAdd;
            close = Math.abs(target / disList.size() - popRatio);
         }
         else  {
            ndx = disList.size() * 2;
         }
      }
      targetRatio = target / disList.size();
      
      // Figure out which party to support.
      if (targetRatio < repRatio)   {
         party = 2;
      }
  
      // Pick the district to flip
      if (repRatio != targetRatio)  {
         ArrayList<District> partyLosing = new ArrayList<District> ();
         for (District dis : disList)  {
            if (party == 1 && dis.getBlueRep() <= .5) {
               partyLosing.add(dis);
            }
            else if (party == 2 && dis.getRedRep() <= .5)   {
               partyLosing.add(dis);
            }
         }
         District toFlip = partyLosing.get(rand.nextInt(partyLosing.size()));
         flipDistrict(party, toFlip);
      } 
   }
   
   /**
      This method flips the supplied district in favor of the under-represented party.
      Returns true if the district was able to be flipped, false otherwise.
   */
   public boolean flipDistrict(int party, District dis)
   {
      boolean madeTrade = true;
      
      while (((party == 1 && dis.getBlueRep() <= .5) ||
       (party == 2 && dis.getRedRep() <= .5)) && madeTrade)   {
         madeTrade = evaluateRandomTrade(dis, party);
         while (cleanUpTrade())  {
            ;
         }
      }
      return madeTrade;
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
            tempVal = tradeValue(Math.abs(trade.theirDis.getBlueTradeRep() - .5),
             trade.perimChange);
            if (tempVal > value) {
               bestTrade = trade;
               value = tempVal;
            }
         }
         else if (trade != null && party == 2)  {
            tempVal = tradeValue(Math.abs(trade.theirDis.getRedTradeRep() - .5),
             trade.perimChange);
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
      ArrayList<District> borderDistricts = temp.getBorderDistricts(disList);
      ArrayList<TradeProp> tradeList = new ArrayList<TradeProp> ();
      
      for (District dis : borderDistricts)  {
         tradeList.add(pickBestTrade(temp, dis, party));
      }
      while (tradeList.size() > 0)  {
         int tradeIndex = rand.nextInt(tradeList.size());
         
         TradeProp randomTrade = tradeList.remove(tradeIndex);
         if (randomTrade != null)   {
            //System.out.println("Random Trade: " + randomTrade);
            makeTrade(randomTrade);
            lastTrade = randomTrade;
            return true;
         }
      }
      return false;
   }
            
   
   /**
      For a specified district, picks the best possible trade between ourDis and theirDis.
      Best possible means lowest perimeter change.
      Returns the proposed trade, or null if no trade is possible.
   */
   private TradeProp pickBestTrade(District ourDis, District theirDis, int party)
   {
      TradeProp bestTrade = null;
      ArrayList<Point> theirBorder = ourDis.getBorderCells(theirDis);
      ArrayList<Point> ourBorder = theirDis.getBorderCells(ourDis);
         
      int perimChange = 10;
         
      for (Point theirCell : theirBorder)  {
         for (Point ourCell : ourBorder)  {
            if (grid[(int) ourCell.getX()][(int) ourCell.getY()] == party ||
             grid[(int) theirCell.getX()][(int) theirCell.getY()] != party) {
               ;
            }
            else  {
               TradeProp trade = new TradeProp(ourCell, theirCell, ourDis, theirDis);
               int curPerim = calcPerim(disList), futPerim;
                  
               makeTrade(trade);
               if (!ourDis.hasIsolation() && !theirDis.hasIsolation()) {
                  futPerim = calcPerim(disList);
                  trade.perimChange = futPerim - curPerim;
                  if (futPerim - curPerim < perimChange) {
                     perimChange = futPerim - curPerim;
                     bestTrade = trade;
                  }
               }
               makeTrade(trade);
            }
         }
      }
      return bestTrade;
   }
   
   /*
      Makes the specified Trade.  Order does not matter.
   */
   public void makeTrade(TradeProp trade)
   {
      if (trade.ourDis.contains(trade.ourCell))   { 
         trade.ourDis.removeSquare(trade.ourCell);
         trade.theirDis.removeSquare(trade.theirCell);
         trade.ourDis.addSquare(trade.theirCell);
         trade.theirDis.addSquare(trade.ourCell);
      }
      else if (trade.ourDis.contains(trade.theirCell))  {
         trade.ourDis.removeSquare(trade.theirCell);
         trade.theirDis.removeSquare(trade.ourCell);
         trade.ourDis.addSquare(trade.ourCell);
         trade.theirDis.addSquare(trade.theirCell);
      }
   }
   
   /**
      Looks for trades that have no impact on representation but result in negative
      perimeter changes.
      Returns true if a clean up trade was made, false otherwise. 
  */
   public boolean cleanUpTrade()
   {
      TradeProp bestTrade = null;
      District ourDis = lastTrade.ourDis, theirDis = lastTrade.theirDis;
      ArrayList<Point> theirBorder = ourDis.getBorderCells(theirDis);
      ArrayList<Point> ourBorder = theirDis.getBorderCells(ourDis);
      int perimChange = 0;
      
      for (Point theirCell : theirBorder)  {
         for (Point ourCell : ourBorder)  {
            if (grid[(int) ourCell.getX()][(int) ourCell.getY()] ==
             grid[(int) theirCell.getX()][(int) theirCell.getY()]) {
             
               TradeProp trade = new TradeProp(ourCell, theirCell, ourDis, theirDis);
               int curPerim = calcPerim(disList), futPerim;
                  
               makeTrade(trade);
               if (!ourDis.hasIsolation() && !theirDis.hasIsolation()) {
                  futPerim = calcPerim(disList);
                  trade.perimChange = futPerim - curPerim;
                  if (futPerim - curPerim < perimChange) {
                     perimChange = futPerim - curPerim;
                     bestTrade = trade;
                  }
               }
               makeTrade(trade);
            }
         }
      }
      if (bestTrade != null)  {
         //System.out.println("Clean Up Trade: " + bestTrade);
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
   
   private double tradeValue(double repToFlip, double perimChange)
   {
      double retVal = 0;
      
      if (repToFlip >= .1) {
         retVal = 100;
      }
      else if (repToFlip >= .05)  {
         retVal = 50;
      }
      retVal -= perimChange;
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
         District newDis = new District(grid, newZone);
         copyList.add(newDis);
      }
   }
}
