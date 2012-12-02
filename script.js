//these functions are colled when an event occurs
var lastKey="NONE";
function onGameStart(handler,automatism){
	automatism.notify("script started, execution of onGameStart");
}

function onPress(handler,automatism,key){
	automatism.notify("script receive the pressure of key "+key+" previous key was "+lastKey);
	lastKey=key;
	return true;
}

function onRelease(handler,automatism,key){
	automatism.notify("script receive the release of key "+key);
	return true;
}
