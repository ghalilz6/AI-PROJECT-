import lejos.hardware.motor.Motor;
import lejos.utility.Delay;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.chassis.*;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;



public class Moteur {
	
	private static final double DIAMETRE = 5.6;
	private MovePilot pilot;
	private float direction =0;
	private RegulatedMotor pinces = new EV3MediumRegulatedMotor(MotorPort.D);
    private boolean pincesOuvertes = true; 
	
	//public void setPincesOuvertes(boolean pincesOuvertes) { 

		//this.pincesOuvertes = pincesOuvertes;
	//}
	
	
	
	public void avancer(double distance) { 
		

		pilot.travel(distance);
		direction += distance/40;
		direction= direction%360;
	}


    public void setPincesOuvertes(boolean ouvertes) {
        if (ouvertes && !pincesOuvertes) {
            pinces.rotate(300);    // ouvrir
        } else if (!ouvertes && pincesOuvertes) {
            pinces.rotate(-300);   // fermer
        }
        this.pincesOuvertes = ouvertes;
    }
    
    public void fermer() {
        pinces.close();
    }
    
    
}
