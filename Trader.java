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
   private double repRatio, popRatio;
   private TradeProp lastTrade = null;
   
   public Trader(int[][] grid, ArrayList<District> disList, int disPop, double popRatio)
   {
      this.grid = grid;
      this.disList = disList;
      this.disPop = disPop;
      this.popRatio = popRatio;
      
      totalPerim = calcPerim(disList);
      //testOne(4);
      //evaluateTrades(3, 2);
   }
   
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
   
   public void makeBestTrade()
   {
      int party = 1;
      double target = 0, close = popRatio, targetRatio;
      
      // Find the target ratio of representation to achieve.
      for (int ndx = 0; ndx < disList.size() * 2; ndx++)  {
         if (Math.abs((target + .5) / disList.size() - popRatio) < close)   {
            target += .5;
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
      Evaluates all possible trades for the specified district for
      the benefit of the specified party. 
      "party" is 1 for the Blue party and 2 for the Red party.
   */ 
   private void evaluateTrades(District temp, int party)
   {
      ArrayList<District> borderDistricts = temp.getBorderDistricts(disList);
      ArrayList<TradeProp> tradeList = new ArrayList<TradeProp> ();
      for (District dis : borderDistricts)  {
         tradeList.add(pickBestTrade(temp, dis, party));
         //System.out.println(tradeList.get(tradeList.size() - 1));
      }
      
      double value = 0, tempVal;
      TradeProp bestTrade = tradeList.get(0);
      for (TradeProp trade : tradeList)   {        
         if (trade != null && party == 1)   {
            tempVal = Math.abs(trade.theirDis.getBlueTradeRep() - .5) * 100 - trade.perimChange;
            if (tempVal > value) {
               bestTrade = trade;
               value = tempVal;
            }
         }
         else if (trade != null && party == 2)  {
            tempVal = Math.abs(trade.theirDis.getRedTradeRep() - .5) * 100 - trade.perimChange;
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
      }    
   }
   
   /**
      For a specified district, picks the best possible trade between temp and dis.
      Best possible means lowest perimeter change.
      Returns the proposed trade, or null if no trade is possible.
   */
   private TradeProp pickBestTrade(District temp, District dis, int party)
   {
      TradeProp bestTrade = null;
      ArrayList<Point> theirBorder = temp.getBorderCells(dis);
      ArrayList<Point> ourBorder = dis.getBorderCells(temp);
         
      int perimChange = 10;
         
      for (Point theirCell : theirBorder)  {
         for (Point ourCell : ourBorder)  {
            if (theirCell.getX() == ourCell.getX() && 
             Math.abs(theirCell.getY() - ourCell.getY()) == 1)   {
               ;
            }
            else if (theirCell.getY() == ourCell.getY() && 
             Math.abs(theirCell.getX() - ourCell.getX()) == 1)   {
               ;
            }
            else if (grid[(int) ourCell.getX()][(int) ourCell.getY()] == party ||
             grid[(int) theirCell.getX()][(int) theirCell.getY()] != party) {
               ;
            }
            else  {
               TradeProp trade = new TradeProp(ourCell, theirCell, temp, dis);
               int curPerim = calcPerim(disList), futPerim;
                  
               makeTrade(trade);
               
               futPerim = calcPerim(disList);
               trade.perimChange = futPerim - curPerim;
               if (futPerim - curPerim < perimChange) {
                  perimChange = futPerim - curPerim;
                  bestTrade = trade;
               }
               makeTrade(trade);
            }
         }
      }
      return bestTrade;
   }
   
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
}
