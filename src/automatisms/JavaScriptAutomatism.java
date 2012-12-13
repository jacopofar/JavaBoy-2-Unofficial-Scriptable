package automatisms;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import machinelearning.NaiveBayes;
import sun.org.mozilla.javascript.Context;
import sun.org.mozilla.javascript.Function;
import sun.org.mozilla.javascript.ScriptableObject;
import emulator.JavaBoy;
/**
 * Run an external JavaScript code through Mozilla Rhino.
 * NOTE: Mozilla Rhino is included in Java framework from JavaSE from version 6 (December 2006).
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
	private Map<String,Runnable>runningTasks=new HashMap<String,Runnable>();
	private ScheduledThreadPoolExecutor taskExecutor=new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
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
		g.runGame("game.gb");
	}


	@Override
	public void onGameStart() {
		System.out.println("Game started, calling onGameStart...");
		functionOnGameStart.call(
				context, scope, scope, new Object[] {gh,this});
	}

	public boolean onButtonPressed(String key){
		//note: we check that it's NOT FALSE, so a script not returning a boolean is considered true
		//in general, a script wants to return true
		return (!functionOnPress.call(
				context, scope, scope, new Object[] {gh,this,key}).equals(Boolean.FALSE));
	}

	public boolean onButtonReleased(String key){
		//note: we check that it's NOT FALSE, so a script not returning a boolean is considered true
		//in general, a script wants to return true
		return (!functionOnRelease.call(
				context, scope, scope, new Object[] {gh,this,key}).equals(Boolean.FALSE));
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

	public NaiveBayes getNBC(int n,String[] classes){
		return new NaiveBayes(n, classes);
	}

	/**
	 * Since JavaScript types are slightly different from Java ones, this method
	 * allow us to peep a little bit for debugging purposes.
	 * */
	public void analyze(Object i){
		System.out.println("received object:"+i);
	}

	/**
	 * Calls a specific JS function, called "task" with a given time frequency expressed in ms
	 * The function will receive the GameHandler and this object just like onGameStart
	 * If the function is still running when it's time for another call, the call is ignored.
	 * The name of the task is used by stopTask to terminate the task, the same function can be 
	 * gave to different tasks.
	 * If there's a task with the given name already running, it will be stopped and replaced by this
	 * @return true if a task with the same name has been replaced, false if the task is new
	 * */
	public boolean startTask(String functionName, int msInterval,String taskName){
		boolean existed=false;
		if(runningTasks.containsKey(taskName)){
			existed=true;
			taskExecutor.remove(runningTasks.get(taskName));
		}

		Function taskFunction = (Function)scope.get(functionName, scope);
		taskExecutor.scheduleAtFixedRate(new JSFunctionRunner(taskFunction,this),msInterval, msInterval,TimeUnit.MILLISECONDS);
		return existed;
	}
	/**
	 * Stop the task with the given name
	 * */
	public void stopTask(String taskName){
		taskExecutor.remove(runningTasks.get(taskName));
	}
	
	
	/**
	 * Wrapper of a Rhino JS function to be called by the scheduler
	 * */
	class JSFunctionRunner implements Runnable{

		private Function function;
		private JavaScriptAutomatism aut;

		public JSFunctionRunner(Function f,JavaScriptAutomatism jsa){
			this.function=f;
			this.aut=jsa;
		}

		@Override
		public void run() {
			function.call(
					context, scope, scope, new Object[] {gh,aut});	
		}
		
	}
}
