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
	
	class TimerListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{

		}
	}
	
	
	class MyKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent event)
		{
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
