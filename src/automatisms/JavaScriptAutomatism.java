package automatisms;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import sun.org.mozilla.javascript.Context;
import sun.org.mozilla.javascript.Function;
import sun.org.mozilla.javascript.ScriptableObject;
import emulator.JavaBoy;
/**
 * Run an external JavaScript code through Rhino.
 * NOTE: Mozilla Rhino in included in Java framework from JavaSE from version 6 (December 2006).
 * If you use a Java framework without it, download it from http://www.mozilla.org/rhino/download.html
 * */
public class JavaScriptAutomatism extends GameBoyListener{
	GameHandle gh;
	String scriptPath;
	private String code;
	private Function functionOnPress;
	private Function functionOnRelease;
	private Function functionOnGameStart;
	private Context context;
	private ScriptableObject scope;
	public JavaScriptAutomatism(GameHandle gh,String scriptPath) throws IOException{
		this.gh=gh;
		this.scriptPath=scriptPath;
		gh.addButtonListener(this);
		code = JavaScriptAutomatism.loadCode(scriptPath);

		context = Context.enter();

		scope = context.initStandardObjects();
		context.evaluateString(scope, code, "script", 1, null);
		
		functionOnPress = (Function)scope.get("onPress", scope);
		functionOnRelease = (Function)scope.get("onRelease", scope);
		functionOnGameStart = (Function)scope.get("onGameStart", scope);
	}


	public static void main(String[] args) throws IOException{
		JavaBoy g=new JavaBoy("");
		Thread p = new Thread(g);
		p.start();
		new JavaScriptAutomatism(new GameHandle(g),"script.js");
	}


	@Override
	public void onGameStart() {
		System.out.println("Game started, calling onGameStart...");
		functionOnGameStart.call(
				context, scope, scope, new Object[] {gh,this});
	}
	
	public boolean onButtonPressed(String key){
		return functionOnPress.call(
				context, scope, scope, new Object[] {gh,this,key}).equals(Boolean.TRUE);
	}
	
	public boolean onButtonReleased(String key){
		return functionOnRelease.call(
				context, scope, scope, new Object[] {gh,this,key}).equals(Boolean.TRUE);
	}
	
	private static String loadCode(String path) throws IOException{
		if(path.startsWith("http://")){
			//TODO manage http load wrt proxies and security
		}
		String res="";
		BufferedReader r = new BufferedReader(new FileReader(path));
		String line;
		while((line=r.readLine())!=null) res+=line+"\n";
		return res;
	}
	/**Helper used to communicate with user*/
	public void notify(String text){
		System.out.println(">"+text);
	}
}