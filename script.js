//these functions are called when an event occurs
var lastKey="NONE";
var recog;
function onGameStart(handler,automatism){
	automatism.notify("script started, execution of onGameStart");
	//let's create the bayes classifier for screen captures
	recog=automatism.getNBC(140*160,["A","B","up","down","right","left","start","select"]);
	
}

function onPress(handler,automatism,key){
	//let's take a screenshot and create a vector from it
	var buf=handler.getScreenshot().getData().getDataBuffer();
	var featureVector=new Array();
	for(var i=0;i<buf.getSize();i++){
		var pixel=buf.getElem(i);
		//pixel is a RGB value, let's just consider whether is over 128 or not (high values of red are seen as negative, due to representation)
		featureVector[i]=(pixel%256>=128?true:false);
	}
	automatism.analyze(featureVector);
	recog.train(featureVector,key);
	automatism.analyze(recog);

	//let's show how a script can intercept keys and store them in variables visible between calls
	automatism.notify("script received the pressure of key "+key+" previous key was "+lastKey);
	lastKey=key;
	return true;
}

function onRelease(handler,automatism,key){
	automatism.notify("script receive the release of key "+key);
	return true;
}
