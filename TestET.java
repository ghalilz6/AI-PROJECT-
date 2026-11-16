import java.util.ArrayList;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.utility.Delay;



public class TestET{

	// test l'ouverture des pinces
	public static void main(String[] args) {
		Moteur moteur = new Moteur();
moteur.setPincesOuvertes(false); // on déclare les pinces FERMÉES au départ

moteur.ouvrirPinces(1200);
moteur.avancer(30);
moteur.fermerPinces();

Delay.msDelay(1000);
moteur.tourner(90); // doit tourner vers la GAUCHE

}
