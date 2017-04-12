//John Rice

import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.io.FileNotFoundException;

public class GerryFrame extends JFrame
{
   private static final int FRAME_WIDTH = 800, FRAME_HEIGHT = 800;
	private GerryComponent scene;
	private boolean trade = false, undo = false, simulation = false, reset = false;
	
	class TimerListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
         if (trade)  {
            scene.makeTrade();
            scene.repaint();
         }
         if (undo) {
            scene.undoTrade();
            scene.repaint();
         }
         if (simulation)   {
            scene.manySolve();
            scene.repaint();
         }
         if (reset)  {
            scene.reset();
            scene.repaint();
         }
         trade = undo = simulation = reset = false;
		}
	}
	
	
	class MyKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent event)
		{
		   String key = KeyStroke.getKeyStrokeForEvent(event).toString();
			key = key.replace("pressed ", "");
			if (key.equals("T"))	{
			   trade = true;
			}
			else if (key.equals("U"))  {
			   undo = true;
			}
			else if (key.equals("S"))  {
			   simulation = true;
			}
			else if (key.equals("X"))  {
			   reset = true;
			}
		}
		
		//Do nothing methods.
		public void keyReleased(KeyEvent event)	{}
		public void keyTyped(KeyEvent event)	{}
		
	}
		
	
	public GerryFrame(int width, int height, int numDis, String datafile) throws FileNotFoundException
	{
		scene = new GerryComponent(width, height, numDis, datafile);
		scene.setFocusable(true);
		
		add(scene);
		
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
		ActionListener listener = new TimerListener();
		
		KeyListener listener2 = new MyKeyListener();
		scene.addKeyListener(listener2);
		
		final int DELAY = 55; //milliseconds between timer ticks.
		Timer t = new Timer (DELAY, listener);
		t.start();
	}
}
