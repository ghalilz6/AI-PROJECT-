import java.util.ArrayList;
import java.util.List;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.robotics.Color;


 
//La classe Fonctionnalite
public class Fonctionnalite {
	//Constantes
	static final double VITESSE_LIN_DEFAUT = 30;   // cm/s
	static final double VITESSE_ROT_DEFAUT = 90;   // degrés/s
	static final int OUVERTURE_PINCES_PALET = 1200;
	static final int OUVERTURE_PINCES_SAISIE = 1200;
	static final int COULEUR_LIGNE_BUT = Color.WHITE;
	static final float SEUIL_OBSTACLE_CM = 15;
	static final double PORTEE_MAX_MUR_CM = 200;
	static final double PORTEE_MIN_PALET_CM = 15;
	static final double PORTEE_MAX_PALET_CM = 100;
	static final double DISTANCE_MUR_CM = 100;     
	//Attributs
	final Moteur moteur;
	final Capteur capteur;
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
	
// lit une mesure du capteur ultrason et la convertit en cm
	private static float lireDistanceCm( SampleProvider source, float[] echantillon){
		source.fetchSample(echantillon,0);
		return echantillon[0]*100;
	}
	
// Tourne de (valeur angle) degrés en mesurant la distance en continu
// renvoie la liste des distances (cm) relevées pendant la rotation	 
	private List<Float> balayerDistances(int angle, boolean afficher) {
		SampleProvider ultrason = capteur.getUltrason().getDistanceMode();
		float[] echantillon = new float[ultrason.sampleSize()];
		List<Float> distances = new ArrayList<Float>();

		moteur.tournerAsync(angle);
		while (moteur.isMoving()) {
			distances.add(lireDistanceCm(ultrason, echantillon));
			if (afficher) {
				int dernier = distances.size() - 1;
				System.out.println(dernier + "- " + distances.get(dernier));
			}
		}
		return distances;
	}
// cherche l'indice de la plus petite distance comprise dans l'intervalle min,max
// retroune l'indice trouvé, si aucune distance est trouvée : retourne -1
	private static int indiceDistanceMinimale(List<Float> distances, double min, double max){
		double meilleure = max;
		int indice = -1;
		for (int j = 0; j < distances.size(); j++){
			float valeur = distances.get(j);
			if (valeur < meilleure && valeur >= min){
				meilleure = valeur;
				indice = j;
			}
		}
		return indice;
	}
	// remet le robot dans sa direction de départ
	private void reprendreCapInitial(){
		moteur.tourner(-moteur.getCap());
	}
	// va jusqu'a la ligne de but, lache le palet et recule en refermant les pinces
	private void deposerPaletSurLigneBut(){
		allerVersCouleur(COULEUR_LIGNE_BUT);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(-10);
		moteur.fermerPinces();
	}
	// avance de la distance donnée en évitant les obstacles à moins de 15cm
	public void avancerAvecEvitement(double distanceCm){
	SampleProvider ultrason = capteur.getUltrason().getDistanceMode();
	float[] echantillon = new float[ultrason.sampleSize()];
	double restant = distanceCm;
	moteur.avancerAsync(restant);
	while (moteur.isMoving()) {
		if (lireDistanceCm(ultrason, echantillon) < SEUIL_OBSTACLE_CM) {
			restant -= moteur.getPilot().getMovement().getDistanceTraveled();
			moteur.stop();
			moteur.tourner(45);
			moteur.avancer(30);
			moteur.tourner(-45);
			restant -= 30 * Math.cos(Math.toRadians(45));
			if (restant <= 0) return;
			moteur.avancerAsync(restant);
			}
		attendre(20);
		}
	}
	// avance jusqu'à détécter la couleur donnée( ou jusqu'à appui sur ENTER)
	public void allerVersCouleur(int couleur){
		if(!capteur.getCouleur().isFloodlightOn()){
			capteur.getCouleur().setFloodlight(true);
		}
		moteur.setVitesseLineaire(30);
		moteur.avancerAsync(300);
		int couleurDetectee = -1;
		while (couleurDetectee != couleur && !Button.ENTER.isDown() && moteur.isMoving()) {
			couleurDetectee = capteur.getCouleur().getColorID();
			attendre(20);
		}
		moteur.stop();
		}
	// avance pinces ouvertes et les referme lorsque le capteur touché est appuyé
	public void saisirPalet(){
		SampleProvider toucher = capteur.getTouche().getTouchMode();
		float[] echantillon = new float[toucher.sampleSize()];

		moteur.ouvrirPinces(OUVERTURE_PINCES_SAISIE);
		moteur.setVitesseLineaire(100);
		moteur.avancer(60);

		while (!Button.ENTER.isDown()) {
			toucher.fetchSample(echantillon, 0);
			if (echantillon[0] == 1.00) {
				moteur.fermerPinces();
				break;
			}
			attendre(1000);
		}
	}
	//apres un but, se place perpendiculairement au mur adverse puis rejoint la ligne noir du milieu et se tourne vers les palets
	public void rejoindreLigneNoire() {
		reprendreCapInitial();
		moteur.setVitesseRotation(36);
		moteur.tourner(-20);

		List<Float> distancesMur = balayerDistances(40, false);
		int indice = indiceDistanceMinimale(distancesMur, Double.NEGATIVE_INFINITY, PORTEE_MAX_MUR_CM);
		if (indice < 0) {
			indice = 0;
		}
		moteur.tourner(-40 + indice * 40.000 / distancesMur.size() + 5);

		moteur.setVitesseRotation(72);
		moteur.tourner(90);
		moteur.setVitesseRotation(36);
		Delay.msDelay(500);

		SampleProvider ultrason = capteur.getUltrason().getDistanceMode();
		float[] echantillon = new float[ultrason.sampleSize()];
		float distanceMurDroit = lireDistanceCm(ultrason, echantillon);

		moteur.avancer(distanceMurDroit - DISTANCE_MUR_CM);
		moteur.tourner(90);
	}
	//se place face au palet le plus proche avance vers lui et l'attrape
	public void viserEtAttraperPalet() {
		moteur.tourner(-55);
		moteur.setVitesseRotation(36);

		List<Float> distances = balayerDistances(110, true);

		int indice = indiceDistanceMinimale(distances, PORTEE_MIN_PALET_CM, PORTEE_MAX_PALET_CM);
		double distancePalet = PORTEE_MAX_PALET_CM;
		if (indice < 0) {
			indice = distances.size();
		} else {
			distancePalet = distances.get(indice);
		}

		moteur.setVitesseRotation(90);
		moteur.tourner(-110 + indice * 110.000 / distances.size() + 10);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(distancePalet + 25);
		moteur.fermerPinces();
	}
	
	//gère le premier palet
	public void marquerPremierPalet() {
		moteur.avancerAsync(60);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.stop();
		moteur.fermerPinces();

		moteur.tourner(100);
		moteur.avancer(20);
		moteur.tourner(-100);

		deposerPaletSurLigneBut();
		nbPalets++;
	}

	
	// Marque les 3 premiers palets.
	public void marquerTroisPremiersPalets() {
		marquerPremierPalet();
		while (nbPalets < 3) {
			rejoindreLigneNoire();
			moteur.avancer(-10);
			viserEtAttraperPalet();
			reprendreCapInitial();
			deposerPaletSurLigneBut();
			nbPalets++;
		}
	}
	
	//Ramasse les 3 palets de la ligne centrale.
	public void marquerPaletsLigneCentrale() {
		while (nbPalets < 6) {
			rejoindreLigneNoire();
			moteur.avancer(45);
			viserEtAttraperPalet();
			reprendreCapInitial();
			deposerPaletSurLigneBut();
		}
	}
	//apres 6 palets marqués, fait des allers-retours sur la ligne blanche de notre camp pour bloquer les autres robots qui arrivent avec un palais
	public void jouerDefense() {
		rejoindreLigneNoire();
		allerVersCouleur(COULEUR_LIGNE_BUT);
		moteur.tourner(90);
		moteur.avancer(DISTANCE_MUR_CM);

		while (true) {
			moteur.avancer(-DISTANCE_MUR_CM * 2);
			moteur.avancer(DISTANCE_MUR_CM * 2);
		}
	}
}
