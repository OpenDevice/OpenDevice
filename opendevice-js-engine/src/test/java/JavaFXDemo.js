var Button = javafx.scene.control.Button;
var HBox = javafx.scene.layout.HBox;
var Scene = javafx.scene.Scene;

var led = new Device(1, DIGITAL);

stage.title = "OpenDevice";

var btnConnect = new Button("Connect");
btnConnect.onAction = function() connect(usb());
//btnConnect.onAction = function() connect(bluetooth("00:13:03:14:19:07"));

var button = new Button("On/Off");
//button.onAction = function()  sendCommand('alertMode', ['anything', 10]);
button.onAction = function() led.toggle();
button.setDisable(true);

var root = new HBox();
root.children.addAll(btnConnect, button);
stage.scene = new Scene(root);
stage.show();

onConnected(function(){
    button.setDisable(false);
});


onConnected(function(){
    button.setDisable(false);
});