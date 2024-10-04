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
 * 
 * PURPOSE: prototype KWh monitoring  
 *
 * Digital Pin layout ARDUINO
 * =============================
 *  2     IRQ 0    - to KW meter SCHELLCOUNT EEM12L-32A On the Steca500
 *  3     IRQ 1    - to KW meter SCHELLCOUNT EEM12L-32A On the Gridfit250LV
 *  21    IRQ 2    - to KW meter 
 *  20    IRQ 3    - to KW meter 
 *
 * SCHELLCOUNT EEM12L-32A
 * This KWh meter gives a pulse every 0.5 Watt IMP/2000. Pulse length: 50ms
 */

// STECA1, Steca500 set 3 panels east. backside of the house.
#define STECA1_INTERRUPT 0
#define STECA1_PIN 2        
unsigned long steca1FallTime;  //Time of front raising
unsigned long steca1RiseTime;  //Time of front falling
String steca1String = "S";                          

// GRIDFIT, Gridfit250LV set 1 panel, east. most inner panel in set of 4 on backside of the house.
#define GRIDFIT_INTERRUPT 1
#define GRIDFIT_PIN 3
unsigned long gridfitFallTime;    //Time of front raising
unsigned long gridfitRiseTime;    //Time of front falling           
String gridfitString = "G";             
            

// STECA2, Steca500 set 3 panels east. Between both roofwindows. 
#define STECA2_INTERRUPT 2
#define STECA2_PIN 21
unsigned long steca2FallTime;    //Time of front raising
unsigned long steca2RiseTime;    //Time of front falling           
String steca2String = "A";      

// STECA3, Steca300 set 2 Photowatt panels 220Wp west. left and right from roof window.
#define STECA3_INTERRUPT 3
#define STECA3_PIN 20
unsigned long steca3FallTime;    //Time of front raising
unsigned long steca3RiseTime;    //Time of front falling           
String steca3String = "B";      

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
// What     : Steca Interrupt handler. 
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Steca1InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse.
 
   if ( digitalRead( STECA1_PIN ) == LOW ) 
   {
      steca1FallTime = millis(); //get time of pulse going down
   }
   else
   {
      steca1RiseTime = millis();  			    			//get time of pulse going up
      interruptTime = steca1RiseTime - steca1FallTime;  	//measure time between down and up

      if ( interruptTime >= MIN_PULSE_WIDTH_SCHC && interruptTime <= MAX_PULSE_WIDTH_SCHC ) 
      {
         Serial.print(steca1String);  
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
// What     : Steca 2 Interrupt handler. 
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Steca2InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse.
 
   if ( digitalRead( STECA2_PIN ) == LOW ) 
   {
      steca2FallTime = millis(); //get time of pulse going down
   }
   else
   {
      steca2RiseTime = millis();     						//get time of pulse going up
      interruptTime = steca2RiseTime - steca2FallTime;  	//measure time between down and up
 
      if ( interruptTime >= MIN_PULSE_WIDTH_VOLT && interruptTime <= MAX_PULSE_WIDTH_VOLT ) 
      {
         Serial.print(steca2String);  
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
// What     : Steca 2 Interrupt handler. 
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void Steca3InterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse.
 
   if ( digitalRead( STECA3_PIN ) == LOW ) 
   {
      steca3FallTime = millis(); //get time of pulse going down
   }
   else
   {
      steca3RiseTime = millis();     						//get time of pulse going up
      interruptTime = steca3RiseTime - steca3FallTime;  	//measure time between down and up

      if ( interruptTime >= MIN_PULSE_WIDTH_VOLT && interruptTime <= MAX_PULSE_WIDTH_VOLT ) 
      {
         Serial.print(steca3String);  
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
// What     : The interrupt routine for the Gridfit250LV
// Remarks  :
// -------------------------------------------------------------------------------------------------------------------
void GridfitInterruptHandler()
{

   unsigned long interruptTime; // Total time of interupt pulse. 
 
   if ( digitalRead( GRIDFIT_PIN ) == LOW ) 
   {
      gridfitFallTime = millis(); //get time of pulse going down
   }
   else
   {
      gridfitRiseTime = millis();  //get time of pulse going up
      interruptTime = gridfitRiseTime - gridfitFallTime;  //measure time between down and up

      if ( interruptTime >= MIN_PULSE_WIDTH_SCHC && interruptTime <= MAX_PULSE_WIDTH_SCHC ) 
      {
         Serial.print(gridfitString);  
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
   // KWH interrupt attached to IRQ 0  = pin2 = Steca
   attachInterrupt( STECA1_INTERRUPT, Steca1InterruptHandler, CHANGE );
   attachInterrupt( STECA2_INTERRUPT, Steca2InterruptHandler, CHANGE );
   attachInterrupt( STECA3_INTERRUPT, Steca3InterruptHandler, CHANGE );
   attachInterrupt( GRIDFIT_INTERRUPT, GridfitInterruptHandler, CHANGE );
   
   
   // 2 3 20 en 21 worden al gebruikt. De rest zetten we naar LOW ( van floating af )
   // kijken of dit het energieverbruik verlaagd van de Arduino
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
   pinMode( STECA1_PIN, INPUT );
   pinMode( STECA2_PIN, INPUT );
   pinMode( STECA3_PIN, INPUT );
   pinMode( GRIDFIT_PIN, INPUT );   
   pinMode( LED, OUTPUT );
  

   digitalWrite( STECA1_PIN, HIGH );
   digitalWrite( STECA2_PIN, HIGH );
   digitalWrite( STECA3_PIN, HIGH );
   digitalWrite( GRIDFIT_PIN, HIGH );

}

void loop() 
{
}

