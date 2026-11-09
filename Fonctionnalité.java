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

		moteur.tournerSync(angle);
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
		for (int j = 0; j < distance.size(); j++){
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
		moteur.tourner(-moteur.getDirections);
	}
	// va jusqu'a la ligne de but, lache le palet et recule en refermant les pinces
	private void deposerPaletSurLigneBut(){
		allerVersCouleur(COULEUR_LIGNE_BUT);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(-10);
		moteur.fermerPinces(OUVERTURE_PINCE_PALET);
	}
	// avance de la distance donnée en évitant les obstacles à moins de 15cm
	public void avancerAvecEvitement(double distanceCm){
		while(moteur.isMoving()){
			ultrason.fetchSample(echantillon,0);
			if (echantillon[0]<SEUIL_OBSTACLE_CM){
				moteur.tourner(45);
				moteur.avancer(30);
				moteur.tourner(-moteur.getDirection());
				moteur.stop();
				moteur.avancerSync(distanceCm);
			}
		}
	}
	// avance jusqu'à détécter la couleur donnée( ou jusqu'à appui sur ENTER)
	public void allerVersCouleur(int couleur){
		if(!capteur.getCouleur().isFloodlightOn()){
			capteur.getCouleur().setFloodlight(true);
		}
		moteur.changerVitlin(30);
		moteur.avancerSync(300);
		int couleurDetectee = -1;
		while (couleurDetectee != couleur && ! Button.ENTER.isDown()) {
			System.out.print(couleurDetectee);
			couleurDetectee = capteur.getCouleur().getColorID();
			attendre(100);
		}
		moteur.stop();
		}
	// avance pinces ouvertes et les referme lorsque le capteur touché est appuyé
	public void saisirPalet(){
		SampleProvider toucher = capteur.getTouche().getTouchMode();
		float[] echantillon = new float[toucher.sampleSize()];

		moteur.ouvrirPinces(OUVERTURE_PINCES_SAISIE);
		moteur.changerVitLin(100);
		moteur.avancer(60);

		while (!Button.ENTER.isDown()) {
			toucher.fetchSample(echantillon, 0);
			if (echantillon[0] == 1.00) {
				moteur.fermerPinces(OUVERTURE_PINCES_SAISIE);
				break;
			}
			attendre(1000);
		}
}
	//apres un but, se place perpendiculairement au mur adverse puis rejoint la ligne noir du milieu et se tourne vers les palets
	public void rejoindreLigneNoire() {
		reprendreCapInitial();
		moteur.changerVitRot(36);
		moteur.tourner(-20);

		List<Float> distancesMur = balayerDistances(40, false);
		int indice = indiceDistanceMinimale(distancesMur, Double.NEGATIVE_INFINITY, PORTEE_MAX_MUR_CM);
		if (indice < 0) {
			indice = 0;
		}
		moteur.tourner(-40 + indice * 40.000 / distancesMur.size() + 5);

		moteur.changerVitRot(72);
		moteur.tourner(90);
		moteur.changerVitRot(36);
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
		moteur.changerVitRot(36);

		List<Float> distances = balayerDistances(110, true);

		int indice = indiceDistanceMinimale(distances, PORTEE_MIN_PALET_CM, PORTEE_MAX_PALET_CM);
		double distancePalet = PORTEE_MAX_PALET_CM;
		if (indice < 0) {
			indice = distances.size();
		} else {
			distancePalet = distances.get(indice);
		}

		moteur.changerVitRot(90);
		moteur.tourner(-110 + indice * 110.000 / distances.size() + 10);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(distancePalet + 25);
		moteur.fermerPinces(OUVERTURE_PINCES_PALET);
	}
	
	 // Se place face au palet le plus proche, avance vers lui et l'attrape.
	public void viserEtAttraperPalet() {
		moteur.tourner(-55);
		moteur.changerVitRot(36);

		List<Float> distances = balayerDistances(110, true);

		int indice = indiceDistanceMinimale(distances, PORTEE_MIN_PALET_CM, PORTEE_MAX_PALET_CM);
		double distancePalet = PORTEE_MAX_PALET_CM;
		if (indice < 0) {
			indice = distances.size();
		} else {
			distancePalet = distances.get(indice);
		}

		moteur.changerVitRot(90);
		moteur.tourner(-110 + indice * 110.000 / distances.size() + 10);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(distancePalet + 25);
		moteur.fermerPinces(OUVERTURE_PINCES_PALET);
	}

	// Se place face au palet le plus proche, avance vers lui et l'attrape.
	public void viserEtAttraperPalet() {
		moteur.tourner(-55);
		moteur.changerVitRot(36);

		List<Float> distances = balayerDistances(110, true);

		int indice = indiceDistanceMinimale(distances, PORTEE_MIN_PALET_CM, PORTEE_MAX_PALET_CM);
		double distancePalet = PORTEE_MAX_PALET_CM;
		if (indice < 0) {
			indice = distances.size();
		} else {
			distancePalet = distances.get(indice);
		}

		moteur.changerVitRot(90);
		moteur.tourner(-110 + indice * 110.000 / distances.size() + 10);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(distancePalet + 25);
		moteur.fermerPinces(OUVERTURE_PINCES_PALET);
	}

	 // Se place face au palet le plus proche, avance vers lui et l'attrape.
	public void viserEtAttraperPalet() {
		moteur.tourner(-55);
		moteur.changerVitRot(36);

		List<Float> distances = balayerDistances(110, true);

		int indice = indiceDistanceMinimale(distances, PORTEE_MIN_PALET_CM, PORTEE_MAX_PALET_CM);
		double distancePalet = PORTEE_MAX_PALET_CM;
		if (indice < 0) {
			indice = distances.size();
		} else {
			distancePalet = distances.get(indice);
		}

		moteur.changerVitRot(90);
		moteur.tourner(-110 + indice * 110.000 / distances.size() + 10);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(distancePalet + 25);
		moteur.fermerPinces(OUVERTURE_PINCES_PALET);
	}
	
//Se place face au palet le plus proche, avance vers lui et l'attrape.
	 
	public void viserEtAttraperPalet() {
		moteur.tourner(-55);
		moteur.changerVitRot(36);

		List<Float> distances = balayerDistances(110, true);

		int indice = indiceDistanceMinimale(distances, PORTEE_MIN_PALET_CM, PORTEE_MAX_PALET_CM);
		double distancePalet = PORTEE_MAX_PALET_CM;
		if (indice < 0) {
			indice = distances.size();
		} else {
			distancePalet = distances.get(indice);
		}

		moteur.changerVitRot(90);
		moteur.tourner(-110 + indice * 110.000 / distances.size() + 10);
		moteur.ouvrirPinces(OUVERTURE_PINCES_PALET);
		moteur.avancer(distancePalet + 25);
		moteur.fermerPinces(OUVERTURE_PINCES_PALET);
	}

}
