#include <Wire.h>
#include <BfButton.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include "BluetoothSerial.h"

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define I2C_ADDRESS  0x3C

float tireSize = 0;
bool isDone = true;
float bodyWeight = 0.0;
float startTime;
volatile bool newData = false;
volatile unsigned long interval = 0;
float rpm = 0;
unsigned long lastUpdate = 0;
float calories = 0;
int k = 0;
float MET;
int btnPin = 33;
int DT = 25; //pin b
int CLK = 26; // pin a
int lastStateA;
int counter = 0;
int lastState;
bool buttonPushed = false;
bool buttonHeld = false;
bool buttonPushedTwice = false;
float lastVelocity = -1;
float lastDistance = -1;
float lastCalories = -1;
int lastCounter = -1;
int scrollIndex = 0;
unsigned long lastScrollTime = 0;
const int scrollDelay = 150;

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
BluetoothSerial BTSerial;
BfButton btn(BfButton::STANDALONE_DIGITAL, btnPin, true, LOW);


void setup() {
  Serial.begin(9600);
  BTSerial.begin("ESP32");

  if(!display.begin(SSD1306_SWITCHCAPVCC, I2C_ADDRESS)) {
    while (true);
  }

  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  pinMode(CLK, INPUT_PULLUP);
  pinMode(DT, INPUT_PULLUP);
  pinMode(btnPin, INPUT_PULLUP);

  startTime = millis(); 
  lastState = digitalRead(CLK);

  btn.onPress(&pressHandler)
      .onDoublePress(&pressHandler)
      .onPressFor(&pressHandler, 1000);


  attachInterrupt(digitalPinToInterrupt(4), hallCounter, RISING);
  attachInterrupt(digitalPinToInterrupt(CLK), encodercounter, CHANGE);
}

void loop() {
  btn.read();
  Serial.println(counter);
  unsigned long currentMillis = millis();

  if (BTSerial.hasClient()){
    isConnected();
  }
  else notConnected();
}

void notConnected(){
  if(!tireSize){
      enterTireSizeDisplay();
    }
    else if(!bodyWeight){
      enterBodyWeightDisplay();
    }
    else{
      calculateRPM();
      calories += calculateCalories();
      if(counter < 40){
        displayData();
        delay(1000);
      }
      else settingsDisplay();
    }
}

void isConnected(){
  calculateRPM();
  displayData();
  processBluetoothData();

  static unsigned long lastSendTime = 0;
  unsigned long timer = millis();

  if (!isDone && timer - lastSendTime >= 100) {
      calories += calculateCalories();

      BTSerial.print("R:"); BTSerial.print(rpm);
      BTSerial.print(" K:"); BTSerial.println(k);

      lastSendTime = timer;
  }
}

void hallCounter() {
  unsigned long currentTime = micros(); 
  interval = currentTime - lastUpdate;
  lastUpdate = currentTime;
  newData = true;
  k++;
}

void encodercounter() {
  if (digitalRead(DT) != digitalRead(CLK)) counter++;
  else counter--;
  if (counter < 0) counter = 0;
}

void processBluetoothData() {
    String receivedData = BTSerial.readStringUntil('\n');

    Serial.print("Primljeno: ");
    Serial.println(receivedData);

    if (receivedData.startsWith("T:")) {
        tireSize = receivedData.substring(2).toFloat();
        Serial.print("Veličina gume: ");
        Serial.println(tireSize);
    } else if (receivedData.startsWith("B:")) {
        isDone = (receivedData.charAt(2) == '1');
        Serial.print("isDone: ");
        Serial.println(isDone ? "true" : "false");
        if (!isDone) startTime = millis();
    } else if (receivedData.startsWith("M:")) {
        bodyWeight = receivedData.substring(2).toFloat();
        Serial.print("Tjelesna masa: ");
        Serial.println(bodyWeight);
    } else {
        Serial.print("Nepoznata naredba: ");  
        Serial.println(receivedData); 
    }
}

void calculateRPM(){
  if (newData) {
        noInterrupts();
        unsigned long timeCopy = interval;
        newData = false;
        interrupts();

        if (timeCopy > 0) {
          rpm = 60000000.0 / timeCopy;
          lastUpdate = millis();
        }
    }
  if (millis() - lastUpdate > 2000) rpm = 0;
}

float calculateCalories() {
  float velocity = calculateVelocity();
  MET = (velocity <= 16) ? 4.0 : (velocity <= 19) ? 6.8 : (velocity <= 22) ? 8.0 : (velocity <= 26) ? 10.0 : 12.0;
  return (velocity > 0) ? bodyWeight * MET / 3600.0 : 0;
}

float calculateExtent() {
  return tireSize * 3.14;
}

float calculateDistance() {
  return calculateExtent() * k / 1000.0;
}

float calculateVelocity() {
  return rpm > 0 ? calculateExtent() * rpm / 3.6 : 0.0;
}

void displayData() {
  float velocity = calculateVelocity();
  float distance = calculateDistance();
  float currentCalories = calories;

  display.clearDisplay();

  display.setCursor(0, 0);
  display.print("Brzina(km/h): ");
  display.println(velocity);

  display.setCursor(0, 10);
  display.print("Distanca(km): ");
  display.println(distance);

  display.setCursor(0, 20);
  display.print("Kalorije(kcal): ");
  display.println(currentCalories);

  display.display();  
}

void settingsDisplay(){
  display.clearDisplay();

  display.setCursor(40,0);
  display.print("Postavke");

  display.setCursor(0,10);
  display.print("Velicina gume: ");
  display.println(tireSize);
  display.setCursor(0,20);
  display.print("Pritisni dva puta");

  display.setCursor(0,30);
  display.print("Kilaza: ");
  display.println(bodyWeight);

  display.setCursor(0,40);
  display.print("Drzi dugme");

  if(buttonHeld){
    bodyWeight = 0;
    counter = 0;
    buttonHeld = false;
  }
  if(buttonPushedTwice){
    tireSize = 0;
    counter = 0;
    buttonPushedTwice = false;
  }
  display.display();
}

void enterTireSizeDisplay() {

  display.clearDisplay();

  display.setCursor(0, 0);
  display.print("Velicina gume: "); 
  display.setCursor(0, 10);
  display.println(counter / 100.0);

  if(buttonPushed){tireSize = counter / 100.0; buttonPushed = false; counter = 0;}
  display.display(); 
} 

void enterBodyWeightDisplay() {

  display.clearDisplay();
  display.setCursor(0, 0);
  display.print("Tjelesna tezina: ");
  display.setCursor(0, 10);
  display.println(counter);

  if(buttonPushed){bodyWeight = counter; buttonPushed = false; counter = 0;}
  display.display(); 
}

void pressHandler(BfButton *btn, BfButton::press_pattern_t pattern) {
  switch (pattern) {
    case BfButton::SINGLE_PRESS:
      buttonPushed = true;
      break;

    case BfButton::DOUBLE_PRESS:
      buttonPushedTwice = true;
      break;

    case BfButton::LONG_PRESS:
      buttonHeld = true;
      break;
  }
}