import java.util.ArrayList;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;


 
//La classe Fonctionnalite
public class Fonctionnalite {
	Moteur m;
	Capteur c;
	boolean arreter = false;
	 int cpt_palet=0;

	

//C'est un constructeur
	public Fonctionnalite() { 

		m = new Moteur();
		c = new Capteur();
	}
}