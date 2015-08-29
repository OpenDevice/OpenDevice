var led = new Device(1, DIGITAL);
var button = new Sensor(5, DIGITAL);

button.onChange(function(){
    if(button.isON()){
        led.on();
    }else{
        led.off();
    }
});

connect(usb());