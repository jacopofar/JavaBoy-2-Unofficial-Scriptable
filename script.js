
var lastKey="NONE";
var recog;

//these functions are called when an event occurs
function onGameStart(handler,automatism){
	automatism.notify("script started, execution of onGameStart");
	//let's create the bayes classifier for screen captures
	recog=automatism.getNBC(140*160,["A","B","up","down","right","left","start","select"]);
	//let's start the automatic player
	automatism.startTask("imitate", 3000,"auto action");
}

function onPress(handler,automatism,key,source){
	//if it's my own key pressure, ignore it
	if(source=="BNC script"){
		automatism.notify("it's this own script action, ignore it (source="+source+")");
		return true;
	}
	//let's take a screenshot and create a vector from it
	var buf=handler.getScreenshot().getData().getDataBuffer();
	var featureVector=new Array();
	var blue=0;
	for(var i=0;i<buf.getSize();i++){
		var pixel=buf.getElem(i);
		//pixel is a RGB value, let's just consider whether is over 128 or not (high values of red are seen as negative, due to representation)
		blue=pixel%256;
		if(blue<0) blue=-blue;
		featureVector[i]=(blue>=128?true:false);
	}
	automatism.analyze(featureVector);
	recog.train(featureVector,key);


	//let's show how a script can intercept keys and store them in variables visible between calls
	automatism.notify("script received the pressure of key "+key+" previous key was "+lastKey);
	lastKey=key;
	return true;
}

function onRelease(handler,automatism,key,source){
	automatism.notify("script receives the release of key "+key+" from "+source);
	return true;
}


//called regularly by the automatism as requested inside onGameStart 
function imitate(handler,automatism){
	automatism.notify("function imitate called");
	//let's create the feature vector fromt the GB screen content
	var buf=handler.getScreenshot().getData().getDataBuffer();
	var featureVector=new Array();
	var blue=0;
	for(var i=0;i<buf.getSize();i++){
		var pixel=buf.getElem(i);
		//pixel is a RGB value, let's just consider whether is over 128 or not (high values of red are seen as negative, due to representation)
		blue=pixel%256;
		if(blue<0) blue=-blue;
		featureVector[i]=(blue>=128?true:false);
	}
	var bestkey=recog.classify(featureVector);
	automatism.notify("bayes decided to press "+bestkey);
	//remember that this key pressure is caused by the script
	isMypress=true;
	handler.pressKey(bestkey,"BNC script");
	//pointless dirty code to wait a moment
	for(var i=0;i<10000;i++){
		i=i+1;
		i=i-1;
	}
	handler.releaseKey(bestkey,"BNC script");
}