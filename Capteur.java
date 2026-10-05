import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;


/**
* La classe Capteur
*/ 
public class Capteur {
	
	private EV3UltrasonicSensor ultrason;
	private EV3TouchSensor touche;
	private EV3ColorSensor couleur;
	
}
// Constructeur 
public Capteurs (){
		ultrason = new EV3UltrasonicSensor(SensorPort.S4);
		touche = new EV3TouchSensor(SensorPort.S3);
		couleur = new EV3ColorSensor(SensorPort.S2);	
	}
// renvoie la touche
public EV3TouchSensor getTouche() {
		return touche;
	}
