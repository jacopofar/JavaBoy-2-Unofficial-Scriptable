package automatisms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import machinelearning.KNN;
import emulator.JavaBoy;

/**
 * Run an external JavaScript code through Rhino
 * */
public class JavaScriptAutomatism extends GameBoyListener{
	GameHandle gh;
	String scriptPath;
	public JavaScriptAutomatism(GameHandle gh,String scriptPath){
		this.gh=gh;
		this.scriptPath=scriptPath;
		gh.addButtonListener(this);
	}


	public static void main(String[] args){
		JavaBoy g=new JavaBoy("");
		Thread p = new Thread(g);
		p.start();
		new JavaScriptAutomatism(new GameHandle(g),"script.js");
	}


	@Override
	public void onGameStart() {
		System.out.println("Game started, preparing script...");
	}
}
