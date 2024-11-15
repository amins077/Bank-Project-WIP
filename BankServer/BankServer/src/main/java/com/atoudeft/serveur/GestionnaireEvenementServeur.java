package com.atoudeft.serveur;

import com.atoudeft.banque.Banque;
import com.atoudeft.banque.CompteBancaire;
import com.atoudeft.banque.CompteCheque;
import com.atoudeft.banque.CompteEpargne;
import com.atoudeft.banque.TypeCompte;
import com.atoudeft.banque.serveur.ConnexionBanque;
import com.atoudeft.banque.serveur.ServeurBanque;
import com.atoudeft.commun.evenement.Evenement;
import com.atoudeft.commun.evenement.GestionnaireEvenement;
import com.atoudeft.commun.net.Connexion;
import com.sun.deploy.util.SyncAccess;

/**
 * Cette classe représente un gestionnaire d'événement d'un serveur. Lorsqu'un serveur reçoit un texte d'un client,
 * il crée un événement à partir du texte reçu et alerte ce gestionnaire qui réagit en gérant l'événement.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementServeur implements GestionnaireEvenement {
    private Serveur serveur;

    /**
     * Construit un gestionnaire d'événements pour un serveur.
     *
     * @param serveur Serveur Le serveur pour lequel ce gestionnaire gère des événements
     */
    public GestionnaireEvenementServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    /**
     * Méthode de gestion d'événements. Cette méthode contiendra le code qui gère les réponses obtenues d'un client.
     *
     * @param evenement L'événement à gérer.
     */
    @Override
    public void traiter(Evenement evenement) {
        Object source = evenement.getSource();
        ServeurBanque serveurBanque = (ServeurBanque)serveur;
        Banque banque;
        ConnexionBanque cnx;
        String msg, typeEvenement, argument, numCompteClient, nip;
        String[] t;

        if (source instanceof Connexion) {
            cnx = (ConnexionBanque) source;
            System.out.println("SERVEUR: Recu : " + evenement.getType() + " " + evenement.getArgument());
            typeEvenement = evenement.getType();
            cnx.setTempsDerniereOperation(System.currentTimeMillis());
            switch (typeEvenement) {
                /******************* COMMANDES GÉNÉRALES *******************/
                case "EXIT": //Ferme la connexion avec le client qui a envoyé "EXIT":
                    cnx.envoyer("END");
                    serveurBanque.enlever(cnx);
                    cnx.close();
                    break;
                case "LIST": //Envoie la liste des numéros de comptes-clients connectés :
                    cnx.envoyer("LIST " + serveurBanque.list());
                    break;
                /******************* COMMANDES DE GESTION DE COMPTES *******************/
                case "NOUVEAU": //Crée un nouveau compte-client :
                    if (cnx.getNumeroCompteClient()!=null) {
                        cnx.envoyer("NOUVEAU NO deja connecte");
                        break;
                    }
                    argument = evenement.getArgument();
                    t = argument.split(":");
                    if (t.length<2) {
                        cnx.envoyer("NOUVEAU NO");
                    }
                    else {
                        numCompteClient = t[0];
                        nip = t[1];
                        banque = serveurBanque.getBanque();
                        if (banque.ajouter(numCompteClient,nip)) {
                            cnx.setNumeroCompteClient(numCompteClient);
                            cnx.setNumeroCompteActuel(banque.getNumeroCompteParDefaut(numCompteClient));
                            cnx.envoyer("NOUVEAU OK " + t[0] + " cree");
                        }
                        else
                            cnx.envoyer("NOUVEAU NO "+t[0]+" existe");
                    }
                    break;

                case "CONNECT":
                    // Vérification si client n'est pas connecté
                    if (cnx.getNumeroCompteClient() != null) {
                        cnx.envoyer("CONNECT NO deja connecte");
                        break;
                    }

                    // Récupération du numéro de compte-client et du NIP
                    argument = evenement.getArgument();
                    t = argument.split(":");
                    if (t.length < 2) {
                        // Si les informations ne sont pas bien formatées, envoyer une réponse d'erreur
                        cnx.envoyer("CONNECT NO format incorrect");
                        break;
                    }

                    numCompteClient = t[0];
                    nip = t[1];
                    banque = serveurBanque.getBanque();

                    // Vérification si un autre client est déjà connecté avec ce compte
                    if (serveurBanque.estCompteConnecte(numCompteClient)) {
                        cnx.envoyer("CONNECT NO deja utilise");
                        break;
                    }

                    // Récupération du compte-client dans la banque et vérification du NIP
                    if (!banque.verifierNip(numCompteClient, nip)) {
                        // Si le compte n'existe pas ou que le NIP est incorrect
                        cnx.envoyer("CONNECT NO compte ou nip incorrect");
                        break;
                    }

                    // Enregistrement du compte-client et du compte chèque par défaut
                    cnx.setNumeroCompteClient(numCompteClient);
                    cnx.setNumeroCompteActuel(banque.getNumeroCompteParDefaut(numCompteClient));

                    // Envoi de la réponse de succès
                    cnx.envoyer("CONNECT OK");
                    break;

                case "EPARGNE":
                    //verifier si compte deja connecter
                    if (cnx.getNumeroCompteClient() != null) {
                        cnx.envoyer("EPARGNE NO");
                        break;
                    }

                    banque = serveurBanque.getBanque();

                    //Verifier si client possede compte Epargne
                    if(banque.clientPossedeCompteEpargne(cnx.getNumeroCompteClient())) {
                        cnx.envoyer("EPARGNE NO");
                    }

                    //genere numero de compte Epargne
                    String nouveauNumeroCompte;
                    do {
                        nouveauNumeroCompte = CompteBancaire.genereNouveauNumero();
                    } while (banque.numeroCompteExistant(nouveauNumeroCompte));


                    //creer compte epargne
                    CompteEpargne nouveauCompte = new CompteEpargne(
                            cnx.getNumeroCompteClient(),
                            TypeCompte.EPARGNE,
                            5.0
                    );
                    nouveauCompte.setNumeroCompte(nouveauNumeroCompte);

                    banque.ajouterCompte(cnx.getNumeroCompteClient(), nouveauCompte);

                    cnx.envoyer("EPARGNE OK " + nouveauNumeroCompte);
                    break;

                    //6.0 - SELECT

                case "SELECT":
                    // Vérification si client n'est pas deja connecté
                    if (cnx.getNumeroCompteClient() == null) {
                        cnx.envoyer("CONNECT NO deja connecte");
                        break;
                    }

                    argument = evenement.getArgument();

                    banque = serveurBanque.getBanque();
                    if (argument.equals("cheque")){
                        cnx.setNumeroCompteActuel(banque.getNumeroCompteParDefaut(cnx.getNumeroCompteClient()));
                    } else if (argument.equals("epargne")) {
                        cnx.setNumeroCompteActuel(banque.getNumeroCompteEpargne(cnx.getNumeroCompteClient()));
                    }
                    else {
                        cnx.envoyer("SELECT NO");
                        break;
                    }

                    cnx.envoyer("SELECT OK");
                    break;

                    //6.1 - CASE DEPOT

                case "DEPOT":
                    if(t.length == 2) {
                        try {
                            double montant = Double.parseDouble(t[1]);
                            crediterCompte(montant);
                            System.out.println("Votre compte à été crédité de " + montant + " unités.");
                        } catch (NumberFormatException e) {
                            System.out.println("Montant invalide: " + t[1]);
                        }
                    } else {
                        System.out.println("Format de commande incorrecte: Utilisation: DEPOT montant.");
                    }
                    break;

                    // 6.2 - RETRAIT

                case "RETRAIT":
                    if(t.length == 2) {
                        try {
                            double montant = Double.parseDouble(t[1]);
                            debiterCompte(montant);
                            System.out.println("Votre compte à été débité de " + montant + " unités.");
                        } catch (NumberFormatException e) {
                            System.out.println("Montant invalide: " + t[1]);
                        }
                    } else {
                        System.out.println("Format de commande incorrecte: Utilisation: RETRAIT montant.");
                    }
                    break;

                    //6.3 - FACTURE

                case "FACTURE":
                    if(t.length == 4) {
                        try {
                            double montant = Double.parseDouble(t[1]);
                            String numFacture = t[2];
                            String description = t[3];
                            //Appel de la méthode payerFacture
                            payerFacture();
                            System.out.println("Votre facture " + numFacture + " a été payée de " + montant + " unités.");
                        } catch (NumberFormatException e) {
                            System.out.println("Montant invalide: " + t[1]);
                        }
                    } else {
                        System.out.println("Format de commande incorrecte. Utilisation: FACTURE montant  numéro de facture description.");
                    }
                    break;

                    // 6.4 - TRANSFER

                case "TRANSFER":
                    if(t.length == 3){
                        try {
                            double montant = Double.parseDouble(t[1]);
                            String numCompteDestinataire = t[2];
                            //Appel de la méthode pour transférer l'argent
                            transfererArgent();
                            System.out.println("Votre transfert de " + montant + " unités vers le compte " + numCompteDestinataire + " a été effectué.");
                        } catch (NumberFormatException e) {
                            System.out.println("Montant invalide: " + t[1]);
                        }
                    } else {
                        System.out.println(" Format de commande incorrecte. Utilisation : TRANSFER montant numéro-compte");
                    }
                    break;
                /******************* TRAITEMENT PAR DÉFAUT *******************/
                default: //Renvoyer le texte recu convertit en majuscules :
                    msg = (evenement.getType() + " " + evenement.getArgument()).toUpperCase();
                    cnx.envoyer(msg);
            }
        }
    }
}