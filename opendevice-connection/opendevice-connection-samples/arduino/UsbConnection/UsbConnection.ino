
#define LED RED_LED // Change LED PIN (in arduino is 13)
// #define LED 13

void setup(){
  Serial.begin(9600);
  pinMode(LED, OUTPUT);  
}

void loop(){

  if (Serial.available() > 0) {

    int value = Serial.read();  // read value
    Serial.print("READ:");
    Serial.println(value);  

    if(value == HIGH || value == LOW ){
      digitalWrite(LED,value);
    }

  }
}



