/*
 *    FILE: KWh monitoring
 *  AUTHOR: Martin Kleinman
 *    DATE: 2010 03 31 
 *
 *
 * Changelog
 * Date        Version    Who    What
 * 27-02-2011  1.0.0.1    MKL    Modified interval to 60 seconds + enhanced printing to comport.
 * 28-02-2011  1.0.0.2    MKL    Pulsecount added as a test
 * 01-03-2011  1.0.0.3    MKL    Added input + HIGH as init.
 * 02-03-2011  1.1.0.0    MKL    Added define block. Added blink of internal led when a pulse is detected.
 * 03-03-2011  1.1.1.0    MKL    Added led on/off pulse detection.
 * 04-03-2011  1.2.0.0    MKL    Improved coding and made variables more clear.
 * 08-03-2011  1.3.0.0    MKL    Rewritten as a core StecaLogger only.
 * 23-03-2011  2.0.0.0    MKL    Added Gridfit
 * 26-02-2013  3.0.0.0    MKL    Pulse length is now checked as well
 * 28-02-2013  3.0.0.1    MKL    Code improvements, removed led status, added code for steca2 and 3.
 * 02-03-2013  3.0.0.2    MKL    Added code again for LED status blink.
 * 08-03-2013  3.0.0.3    MKL    Two new kWhmeters have a S0 pulse of 30ms
 * 10-03-2013  3.0.0.4    MKL    Modified ms gap, made gap a bit bigger to ensure we never leave out a correct pulse.
 * 20-02-2014  3.1.0.0    MKL    Trying to reduce power consumption for the MEGA by disabling floating digital pins.
 * 02-01-2015  3.2.0.0	  MKL	 Updated variable names, updated code.
 * 
 * PURPOSE: prototype KWh monitoring  
 *
 * Digital Pin layout ARDUINO
 * =============================
 *  2     IRQ 0    - to KW meter 
 *  3     IRQ 1    - to KW meter 
 *  21    IRQ 2    - to KW meter 
 *  20    IRQ 3    - to KW meter 
 *
 * SCHELLCOUNT EEM12L-32A
 * This KWh meter gives a pulse every 0.5 Watt IMP/2000. Pulse length: 50ms
 */

// Inverter one
#define INVERTER1_INTERRUPT 0
#define INVERTER1_PIN 2        
unsigned long inverter1FallTime;  //Time of front raising
unsigned long inverter1RiseTime;  //Time of front falling
String inverter1String = "1";                          

// Inverter two
#define INVERTER2_INTERRUPT 1
#define INVERTER2_PIN 3
unsigned long inverter2FallTime;    //Time of front raising
unsigned long inverter2RiseTime;    //Time of front falling           
String inverter2String = "2";             
            

// Inverter three
#define INVERTER3_INTERRUPT 2
#define INVERTER3_PIN 21
unsigned long inverter3FallTime;    //Time of front raising
unsigned long inverter3RiseTime;    //Time of front falling           
String inverter3String = "3";      

// Inverter four
#define INVERTER4_INTERRUPT 3
#define INVERTER4_PIN 20
unsigned long inverter4FallTime;    //Time of front raising
unsigned long inverter4RiseTime;    //Time of front falling           
String inverter4String = "B";      

// SCHELLCOUNT INFO
unsigned long MIN_PULSE_WIDTH_SCHC = 20;        // minimal pulse width SCHELLCOUNT;
unsigned long MAX_PULSE_WIDTH_SCHC = 55;        // maximal pulse width SCHELLCOUNT;

// VOLTCRAFT PULSE
unsigned long MIN_PULSE_WIDTH_VOLT = 20;        // minimal pulse width VOLTCRAFT;
unsigned long MAX_PULSE_WIDTH_VOLT = 55;        // maximal pulse width VOLTCRAFT;

#define LED 13
boolean LED_STATUS = false;           // led status.

boolean DEBUG = false;

// -------------------------------------------------------------------------------------------------------------------
// Author   : Martin Kleinman
// What     : Change status LED ( status )
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void ChangeLedStatus()
{
  if ( LED_STATUS )
  {
    digitalWrite( LED, LOW );
    LED_STATUS = false;
  }
  else
  {
    digitalWrite( LED, HIGH );
    LED_STATUS = true;
  }  
}


// -------------------------------------------------------------------------------------------------------------------
// Author   : Martin Kleinman
// What     : Inverter one interrupt handler
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Inverter1InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse.
 
   if ( digitalRead( INVERTER1_PIN ) == LOW ) 
   {
      inverter1FallTime = millis(); //get time of pulse going down
   }
   else
   {
      inverter1RiseTime = millis();  			    			//get time of pulse going up
      interruptTime = inverter1RiseTime - inverter1FallTime;  	//measure time between down and up

      if ( interruptTime >= MIN_PULSE_WIDTH_SCHC && interruptTime <= MAX_PULSE_WIDTH_SCHC ) 
      {
         Serial.print(inverter1String);  
         ChangeLedStatus();
      } else {
        if ( DEBUG ) { 
           // pulse was too short or too long.
           Serial.println( interruptTime );
        }  
        
      }
   } 
}

// -------------------------------------------------------------------------------------------------------------------
// Author   : Martin Kleinman
// What     : Inverter two interrupt handler
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Inverter2InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse. 
 
   if ( digitalRead( INVERTER2_PIN ) == LOW ) 
   {
      inverter2FallTime = millis(); //get time of pulse going down
   }
   else
   {
      inverter2RiseTime = millis();  //get time of pulse going up
      interruptTime = inverter2RiseTime - inverter2FallTime;  //measure time between down and up

      if ( interruptTime >= MIN_PULSE_WIDTH_SCHC && interruptTime <= MAX_PULSE_WIDTH_SCHC ) 
      {
         Serial.print(inverter2String);  
         ChangeLedStatus();
      } else {
        if ( DEBUG ) { 
           // pulse was too short or too long.
           Serial.println( interruptTime );
        }  
      }
   } 
}



// -------------------------------------------------------------------------------------------------------------------
// Author   : Martin Kleinman
// What     : Inverter three interrupt handler
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Inverter3InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse.
 
   if ( digitalRead( INVERTER3_PIN ) == LOW ) 
   {
      inverter3FallTime = millis(); //get time of pulse going down
   }
   else
   {
      inverter3RiseTime = millis();     						//get time of pulse going up
      interruptTime = inverter3RiseTime - inverter3FallTime;  	//measure time between down and up
 
      if ( interruptTime >= MIN_PULSE_WIDTH_VOLT && interruptTime <= MAX_PULSE_WIDTH_VOLT ) 
      {
         Serial.print(inverter3String);  
         ChangeLedStatus();
      } else {
        if ( DEBUG ) { 
           // pulse was too short or too long.
          Serial.println( interruptTime );
        }  
      }
   } 
}

// -------------------------------------------------------------------------------------------------------------------
// Author   : Martin Kleinman
// What     : Inverter four interrupt handler
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Inverter4InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse.
 
   if ( digitalRead( INVERTER4_PIN ) == LOW ) 
   {
      inverter4FallTime = millis(); //get time of pulse going down
   }
   else
   {
      inverter4RiseTime = millis();     						//get time of pulse going up
      interruptTime = inverter4RiseTime - inverter4FallTime;  	//measure time between down and up

      if ( interruptTime >= MIN_PULSE_WIDTH_VOLT && interruptTime <= MAX_PULSE_WIDTH_VOLT ) 
      {
         Serial.print(inverter4String);  
         ChangeLedStatus();
      } else {
        if ( DEBUG ) { 
           // pulse was too short or too long.
           Serial.println( interruptTime );
        }  
     }
   } 
}




void setup()
{
   Serial.begin(9600);
   attachInterrupt( INVERTER1_INTERRUPT, Inverter1InterruptHandler, CHANGE );
   attachInterrupt( INVERTER3_INTERRUPT, Inverter3InterruptHandler, CHANGE );
   attachInterrupt( INVERTER4_INTERRUPT, Inverter4InterruptHandler, CHANGE );
   attachInterrupt( INVERTER2_INTERRUPT, Inverter2InterruptHandler, CHANGE );
   
   
   pinMode( 0 , INPUT );
   pinMode( 1 , INPUT );

   // PWM pins
   pinMode( 4 , INPUT );
   pinMode( 5 , INPUT );
   pinMode( 6 , INPUT );
   pinMode( 7 , INPUT );
   pinMode( 8,  INPUT );
   pinMode( 9 , INPUT );
   pinMode( 10 , INPUT );
   pinMode( 11 , INPUT );
   pinMode( 12 , INPUT );

   // IO pins
   pinMode( 14 , INPUT );
   pinMode( 15 , INPUT );
   pinMode( 16 , INPUT );
   pinMode( 17 , INPUT );
   pinMode( 18 , INPUT );
   pinMode( 19 , INPUT );

   // bulk pins 
   pinMode( 22 , INPUT );
   pinMode( 23 , INPUT );
   pinMode( 24 , INPUT );
   pinMode( 25 , INPUT );
   pinMode( 26 , INPUT );
   pinMode( 27 , INPUT );
   pinMode( 28 , INPUT );
   pinMode( 29 , INPUT );
   pinMode( 30 , INPUT );
   pinMode( 31 , INPUT );
   pinMode( 32 , INPUT );
   pinMode( 33 , INPUT );
   pinMode( 34 , INPUT );
   pinMode( 35 , INPUT );
   pinMode( 36 , INPUT );
   pinMode( 37 , INPUT );
   pinMode( 38 , INPUT );
   pinMode( 39 , INPUT );
   pinMode( 40 , INPUT );
   pinMode( 41 , INPUT );
   pinMode( 42 , INPUT );
   pinMode( 43 , INPUT );
   pinMode( 44 , INPUT );
   pinMode( 45 , INPUT );
   pinMode( 46 , INPUT );
   pinMode( 47 , INPUT );
   pinMode( 48 , INPUT );
   pinMode( 49 , INPUT );
   pinMode( 50 , INPUT );
   pinMode( 51 , INPUT );
   pinMode( 52 , INPUT );
   pinMode( 53 , INPUT );
   
   digitalWrite( 0 , LOW );
   digitalWrite( 1 , LOW );

   // PWM pins
   digitalWrite( 4 , LOW );
   digitalWrite( 5 , LOW );
   digitalWrite( 6 , LOW );
   digitalWrite( 7 , LOW );
   digitalWrite( 8,  LOW );
   digitalWrite( 9 , LOW );
   digitalWrite( 10 , LOW );
   digitalWrite( 11 , LOW );
   digitalWrite( 12 , LOW );

   // IO pins
   digitalWrite( 14 , LOW );
   digitalWrite( 15 , LOW );
   digitalWrite( 16 , LOW );
   digitalWrite( 17 , LOW );
   digitalWrite( 18 , LOW );
   digitalWrite( 19 , LOW );

   // bulk pins 
   digitalWrite( 22 , LOW );
   digitalWrite( 23 , LOW );
   digitalWrite( 24 , LOW );
   digitalWrite( 25 , LOW );
   digitalWrite( 26 , LOW );
   digitalWrite( 27 , LOW );
   digitalWrite( 28 , LOW );
   digitalWrite( 29 , LOW );
   digitalWrite( 30 , LOW );
   digitalWrite( 31 , LOW );
   digitalWrite( 32 , LOW );
   digitalWrite( 33 , LOW );
   digitalWrite( 34 , LOW );
   digitalWrite( 35 , LOW );
   digitalWrite( 36 , LOW );
   digitalWrite( 37 , LOW );
   digitalWrite( 38 , LOW );
   digitalWrite( 39 , LOW );
   digitalWrite( 40 , LOW );
   digitalWrite( 41 , LOW );
   digitalWrite( 42 , LOW );
   digitalWrite( 43 , LOW );
   digitalWrite( 44 , LOW );
   digitalWrite( 45 , LOW );
   digitalWrite( 46 , LOW );
   digitalWrite( 47 , LOW );
   digitalWrite( 48 , LOW );
   digitalWrite( 49 , LOW );
   digitalWrite( 50 , LOW );
   digitalWrite( 51 , LOW );
   digitalWrite( 52 , LOW );
   digitalWrite( 53 , LOW );   
 
   // echte init voor ons werk.
   pinMode( INVERTER1_PIN, INPUT );
   pinMode( INVERTER3_PIN, INPUT );
   pinMode( INVERTER4_PIN, INPUT );
   pinMode( INVERTER2_PIN, INPUT );   
   pinMode( LED, OUTPUT );
  

   digitalWrite( INVERTER1_PIN, HIGH );
   digitalWrite( INVERTER3_PIN, HIGH );
   digitalWrite( INVERTER4_PIN, HIGH );
   digitalWrite( INVERTER2_PIN, HIGH );

}

void loop() 
{
}

