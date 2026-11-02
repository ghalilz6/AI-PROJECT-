import java.util.ArrayList;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;


 
//La classe Fonctionnalite
public class Fonctionnalite {
	final Moteur moteur;
	final Capteur capteur;
	boolean arretDemande = false;
	int nbPalets = 0;

	

//C'est un constructeur
	public Fonctionnalite() { 

		moteur = new Moteur();
		capteur = new Capteur();
	}

//Pause qui ignore proprement l'interruption
	private static void attendre(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
