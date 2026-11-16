import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class Moteur {

	private static final double DIAMETRE_ROUE_CM = 5.6;
	private static final double DEMI_ECARTEMENT_CM = 6.2; 
	private static final int VITESSE_PINCES = 1000;      
	private static final int OUVERTURE_PINCES_DEFAUT = 1200;

	private final MovePilot pilote;

    // angle du robot par rapport à sa direction de départ, entre -180 et 180
	// (0 = face au but adverse, positif = vers la gauche)
	private double cap = 0;

	private boolean pincesOuvertes = false;
	private int ouverturePinces = OUVERTURE_PINCES_DEFAUT; // degrés réellement parcourus à l'ouverture

	//Constructeur  
	public Moteur() {
			Wheel roueGauche = WheeledChassis.modelWheel((RegulatedMotor) Motor.B, DIAMETRE_ROUE_CM).offset(-DEMI_ECARTEMENT_CM);
			Wheel roueDroite = WheeledChassis.modelWheel((RegulatedMotor) Motor.A, DIAMETRE_ROUE_CM).offset(DEMI_ECARTEMENT_CM);
			Chassis chassis = new WheeledChassis(new Wheel[] { roueGauche, roueDroite }, WheeledChassis.TYPE_DIFFERENTIAL);
			pilote = new MovePilot(chassis);
		}

	//renvoie l'objet MovPilot
	public MovePilot getPilot() {
			return pilote;
		}

	// ---------- Cap ----------

	// ajoute angle au cap et le ramène entre -180 et 180
	// (ainsi tourner(-getCap()) prend toujours le chemin le plus court)
	private void ajouterAuCap(double angle) {
		cap = (cap + angle) % 360;
		if (cap > 180) {
			cap -= 360;
		} else if (cap <= -180) {
			cap += 360;
		}
	}
	
	//Renvoie le cap en degrés (0 si le robot regarde le but adverse).
	public double getCap() {
	return cap;
	}
	// Force le cap en degrés (utile pour recaler le robot, par exemple contre un mur).
	public void setCap(double cap) {
	this.cap = 0;
	ajouterAuCap(cap);
	}

	// Déplacements du robot 

	public void tourner(double angle) {
		pilote.rotate(angle);
		ajouterAuCap(angle);
	}

	public void tournerAsync(double angle) {
		pilote.rotate(angle, true);
		ajouterAuCap(angle);
	}

	public void avancer(double distanceCm) {
		pilote.travel(distanceCm);
	}

	public void avancerAsync(double distanceCm) {
		pilote.travel(distanceCm, true);
	}

	public void avancerEnContinu() {
		pilote.forward();
	}

	public boolean isMoving() {
		return pilote.isMoving();
	}

	public void stop() {
		pilote.stop();
	}
	
	// ---------- Vitesses ----------
	
	//vitesse linéaire en cm/s
	public void setVitesseLineaire(double cmParSeconde) {
		pilote.setLinearSpeed(cmParSeconde);
	}
	
	//vitesse de rotation en degrés/s
	public void setVitesseRotation(double degParSeconde) {
		pilote.setAngularSpeed(degParSeconde);
	}
	
	// ---------- Pinces ----------

	public void setPincesOuvertes(boolean pincesOuvertes) {
		this.pincesOuvertes = pincesOuvertes;
	}

	public void ouvrirPinces(int degres) {
		if (pincesOuvertes) {
		return;
		}
		Motor.D.setSpeed(VITESSE_PINCES);
		Motor.D.rotate(degres); // bloquant : rend la main quand le mouvement est fini
		ouverturePinces = degres;
		pincesOuvertes = true;
	}

	public void fermerPinces() {
		if (!pincesOuvertes) {
		return;
		}
		Motor.D.setSpeed(VITESSE_PINCES);
		Motor.D.rotate(-ouverturePinces);
		pincesOuvertes = false;
	}
}

	
	
