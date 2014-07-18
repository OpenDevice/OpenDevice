int sensVal;           // for raw sensor values 
float filterVal;       // this determines smoothness  - .0001 is max  1 is off (no smoothing)
float smoothedVal;     // this holds the last loop value just use a unique variable for every different sensor that needs smoothing

float smoothedVal2;   // this would be the buffer value for another sensor if you needed to smooth two different sensors - not used in this sketch


int i, j;              // loop counters or demo     

void setup()
{
  Serial.begin(9600);
  //Serial.println("Start ");
  
  filterVal = .8; 
}


void loop()
{ 

      sensVal = analogRead(5);  
      smoothedVal =  smooth(sensVal, filterVal, smoothedVal);   // second parameter determines smoothness  - 0 is off,  .9999 is max smooth 

      Serial.print(sensVal);
      Serial.print(",");
      Serial.print(smoothedVal, DEC);
      Serial.print(",");
      Serial.println(analogRead(0), DEC);
      delay(30); 
    
}

int smooth(int data, float filterVal, float smoothedVal){


  if (filterVal > 1){ // make sure param's are within range
    filterVal = .99;
  }else if (filterVal <= 0){ // make sure param's are within range
    filterVal = 0;
  }

  smoothedVal = (data * (1 - filterVal)) + (smoothedVal  *  filterVal);

  return (int)smoothedVal;
}

