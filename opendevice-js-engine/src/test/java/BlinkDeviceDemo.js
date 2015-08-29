
var led = new Device(1, DIGITAL);

connect(usb());
//connect(bluetooth("00:13:03:14:19:07"));

while(true){
    led.on();
    delay(600);
    led.off();
    delay(600);
}