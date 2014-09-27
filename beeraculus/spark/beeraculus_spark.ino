int light1, light2, light3 = 0;

void setup() {
    
    Spark.variable("light1", &light1, INT);
    Spark.variable("light2", &light2, INT);
    Spark.variable("light3", &light3, INT);
    pinMode(A0, INPUT);
    pinMode(A1, INPUT);
    pinMode(A2, INPUT);

}

void loop() {
    
    light1 = analogRead(A0);
    light2 = analogRead(A1);
    light3 = analogRead(A2);

}