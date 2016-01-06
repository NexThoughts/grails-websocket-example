<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'admin.label', default: 'Admin')}" />
		<title><g:message code="default.create.label" args="[entityName,BAH,BAH]" /></title>
	</head>
	<body>	
	<form>
	<input id="textMessage" type="text">
	<input type="button" value="send" onClick="sendMessage();">
	</form>
	<br>
	<textarea id="messagesTextarea" rows="10" cols="50">
	</textarea>
	
	<script type="text/javascript">
		var webSocket=new WebSocket("ws://localhost:8080/grails-websocket-example/chatroomServerEndpoint");
		/**
		 * Binds functions to the listeners for the websocket.
		 *
     **/
		var messagesTextarea=document.getElementById("messagesTextarea");
//    webSocket.onopen = function(event){
//      // For reasons I can't determine, onopen gets called twice
//      // and the first time event.data is undefined.
//      // Leave a comment if you know the answer.
//     webSocket.send('ping')
//    };
    //data is inbuilt property in websocket
		webSocket.onmessage=function(message) {
			console.log('on message called');
			var jsonData=JSON.parse(message.data)
			if (jsonData.message!=null) {messagesTextarea.value +=jsonData.message+"\n";}
		}
		//webSocket.onopen=function(message) {processOpen(message);};
		webSocket.onclose=function(message) {processClose(message);};
		webSocket.onerror=function(message) {processError(message);};

		function processOpen(message) {
			console.log('on open called');
			messagesTextarea.value +=" Server Connect.... "+"\n";
		}

		function sendMessage() {
      console.log('on send click');
			if (textMessage.value!="close") {
				webSocket.send(textMessage.value);
				textMessage.value="";
			}else {
				webSocket.close();
			}	
		}
		function processClose(message) {
      console.log('on close called');
			webSocket.send("Client disconnected......");
			messagesTextarea.value +="Server Disconnected... "+"\n";
		}
		function processError(message) {
			messagesTextarea.value +=" Error.... \n";
		}
	</script>
	</body>
	</html>