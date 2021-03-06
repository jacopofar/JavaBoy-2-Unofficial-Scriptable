package automatisms;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import emulator.JavaBoy;

/**
 * Define a set of methods to interact with a game,
 * an automatism should use only an instance of this class instead of using directly the emulator classes.
 * */
public class GameHandle{
	private ArrayList<GameBoyListener> listeners=new ArrayList<GameBoyListener>();
	private JavaBoy g;

	GameHandle(JavaBoy j){
		g=j;
		g.addHandle(this);
	}

	public BufferedImage getScreenshot(){
		return g.getScreenShot();
	}
	/**
	 * Simulate the pression of a key
	 * NOTE: it will call the listeners just like a human-caused pression
	 * */
	public void pressKey(String key,String source){
		g.sendButtonPress(key,source);
	}
	/**
	 * Simulate the release of a key
	 * NOTE: it will call the listeners just like a human-caused pression
	 * */
	public void releaseKey(String key,String source){
		g.sendButtonRelease(key,source);
	}

	public void addButtonListener(GameBoyListener l){
		listeners.add(l);
	}

	public void removeButtonListener(GameBoyListener l){
		listeners.remove(l);
	}

	/**
	 * Remove all listeners and unbind the handle from the JavaBoy
	 * */
	public void quit(){
		listeners.clear();
		g.removeHandle(this);
	}
	
	//TODO define the behavior when a listener returns false, wrt the order of listeners
	public boolean onPressure(String button, String source) {
		for(GameBoyListener l:listeners){
			if(!l.onButtonPressed(button,source))return false;
		}
		return true;
	}
	public boolean onRelease(String button, String source) {
		for(GameBoyListener l:listeners){
			if(!l.onButtonReleased(button,source))return false;
		}
		return true;
	}

	public void onGameStart() {
		for(GameBoyListener l:listeners){
			l.onGameStart();
		}
		
	}


}
