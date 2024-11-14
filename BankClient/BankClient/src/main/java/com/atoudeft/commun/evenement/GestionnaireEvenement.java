package com.atoudeft.commun.evenement;

import com.atoudeft.client.Client;

/**
 * Cette interface représente un gestionnaire d'événement.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
@FunctionalInterface
public interface GestionnaireEvenement {
	/**
	* Méthode de gestion de l'événement.
	*
	* @param evenement L'événement à gérer.
	*/
	void traiter(Evenement evenement);
	Client client;
	switch (Evenement) {
		case "END":
			client.deconnecter();
			break;
		case "LIST":
			String arg = evenement.getArgument();
			membres = arg.split(":");
			System.out.println("\t\t"+membres.length+" personnes dans le salon :");
			for (String s:membres)
				System.out.println("\t\t\t- "+s);
			break;
		default:
			System.out.println("\t\t"+evenement.getType()+" "+evenement.getArgument());
	}
}
