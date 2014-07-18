var app = app || {};

app.VoiceCommand = Backbone.Model.extend({
  
  /**
  * 
  * @param {String} text
  */
  analize: function(text) {
  	
  	console.log('Analizar:' + text);
  	
  	var CMD_INVALID_MSG = "Comando não reconecido. <br/> Tente: <b><font color='red'>casa</font> ligar luz 1</b>."
  	var CMD_START = "casa";
  	var CMD_CONTROL = "ligar|desligar";
  	
  	CMD_INVALID_MSG += "<br/>Comando Atual:" + text +"<br/>"
  	
  	// Processed commands (TODO: definir melhor um algoritmo de processamento dos comandos)
  	// Criar uma CommandTextProcessor.. , que pode ou não começar com CASA, onde ele tambem
  	// seria usado para o GTALK...
  	
  	var deviceName = "";
  	var deviceCmd = "";
  	var errorMsg = "";
  	
  	// Validar mensagem..
  	if(!text || text.length == 0 ){
  		$.jGrowl(CMD_INVALID_MSG, {position: "bottom-right"}); // TODO; abstratir mensagem: Msg.show();
  		return;
  	}
  	
  	text = text.toLowerCase();
  	var words = text.match(/\S+/g)
  	
  	// Checa restrição básica..
  	// Ex.: casa ligar luz 1
  	if(words.length >= 3){
  		
  		var valid = true;
  		
//  		if(CMD_START.indexOf(words[0])  !=-1){

  			// Detectar a ação: LIGAR, DESLIGAR
  			if(CMD_CONTROL.indexOf(words[1])  !=-1){
  				deviceCmd = words[1];
  			}else{
  				valid = false;
  				errorMsg = 'ERRO; Comando não reconheceu uma ação:[' + CMD_CONTROL + "], ATUAL:" + words[1];
  			}
  			
  			// Detectar o dispositivo: Luz X
  			// TODO: criar um findDeviceByName, que seja bem flexível...
  			deviceName = words[2] + "-" + words[3];
  			
//  		}else{
//  			errorMsg = 'ERRO; Comando não iniciou com: [' + CMD_START + "], ATUAL:" + words[0];
//  			valid = false;
//  		}
  		
  		// Execute...
  		
  		if(valid){
  			
  			console.log('Comando Processado:' + deviceName + ", ação: " + deviceCmd);
  			$.jGrowl("<font color='green'>Comando Processado:</font>" + deviceName + ", ação: " + deviceCmd, {sticky: true, position: "bottom-right"});
  			
  			var cmdValue = (deviceCmd == 'ligar' ? '1' : '0');
  			
  			var deviceDOM = $($('a.device-view').get(0));
  			var spanDOM = $('span.device-view-value', deviceDOM);
  			var iconDOM = $('span.device-view-icon', deviceDOM);
  			spanDOM.removeClass('on');
  			spanDOM.removeClass('off')
  			
  			spanDOM.addClass((cmdValue == 1 ? "on" : "off" ));
  			spanDOM.text((cmdValue == 1 ? "on" : "off" ).toUpperCase());
  			
  			if(cmdValue == 1) iconDOM.attr('class', 'device-view-icon mws-ic ic-lightbulb');
  			if(cmdValue == 0) iconDOM.attr('class', 'device-view-icon mws-ic ic-lightbulb-off');
  			
  			var deviceID = 1;

			// Notify Listeners...  			
  			app.mainApp.trigger(app.Constants.ACTION_SEND, app.CommandType.ON_OFF, deviceID, cmdValue);
  			
  		}else{
  			console.log(errorMsg);
  			$.jGrowl(CMD_INVALID_MSG  + errorMsg, {sticky: true, position: "bottom-right"}); 
  		}
  		
  		
  	}else{
  		$.jGrowl(CMD_INVALID_MSG, {sticky: true, position: "bottom-right"}); 
  	}
  	
  },
  init: function() { // TODO: não seria melhor usar o constructor / initialize ?
  	    console.log("init voice controller !");

        try {
            var recognition = new webkitSpeechRecognition();
        } catch(e) {
            var recognition = Object;
        }
        
        recognition.continuous = true;
        recognition.interimResults = true;
        
        var this_ = this;
        
        recognition.onresult = function (event) {
        	console.log('----recognition.onresult---');
        	for (var i = event.resultIndex; i < event.results.length; ++i) {
                    console.log("["+event.results[i].isFinal + "] - " + event.results[i][0].transcript);
                if (event.results[i].isFinal) {
                	this_.analize(event.results[i][0].transcript);
                } else {
                	
                }
            }
        };
        
        recognition.onend = function() {
        	console.log('recognition.on end');
        };
        
        recognition.start();
  },
  stop: function() {
  	 if(recognition) recognition.stop();
  }
});