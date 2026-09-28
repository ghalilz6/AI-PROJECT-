import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.utility.Delay;



public class ET {

	public static void main(String[] args) {
		Moteur moteur = new Moteur();
        moteur.setPincesOuvertes(false); //false ferme les pinces
        moteur.avancer(300);
        moteur.fermer(); 
    }

}
