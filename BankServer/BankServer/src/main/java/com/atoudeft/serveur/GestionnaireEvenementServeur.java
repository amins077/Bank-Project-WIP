package com.atoudeft.serveur;

import com.atoudeft.banque.*;
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
        Banque banque = serveurBanque.getBanque();
        ConnexionBanque cnx;
        String msg, typeEvenement, numCompteClient, nip;
        String argument = evenement.getArgument();
        String[] t = argument.split(":");;

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
                        cnx.envoyer("EPARGNE NO non connecte");
                        break;
                    }

                    banque = serveurBanque.getBanque();

                    //Verifier si client possede compte Epargne
                    if(banque.clientPossedeCompteEpargne(cnx.getNumeroCompteClient())) {
                        cnx.envoyer("EPARGNE NO compte epargne existe deja");
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

                    banque.ajouter(cnx.getNumeroCompteClient(), nouveauCompte);

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
                    if (cnx.getNumeroCompteClient() == null) {
                        cnx.envoyer("DEPOT NO non connecte");
                        break;
                    }
                    if (t.length != 2) {
                        cnx.envoyer("DEPOT NO format incorrect");
                        break;
                    }
                    try {
                        double montant = Double.parseDouble(t[1]);
                        CompteBancaire compte = banque.getCompte(cnx.getNumeroCompteActuel());
                        if (compte != null) {
                            compte.crediter(montant);
                            cnx.envoyer("DEPOT OK " + montant);
                        } else {
                            cnx.envoyer("DEPOT NO compte inexistant");
                        }
                    } catch (NumberFormatException e) {
                        cnx.envoyer("DEPOT NO montant invalide");
                    }
                    break;

                    // 6.2 - RETRAIT

                case "RETRAIT":
                    if (cnx.getNumeroCompteClient() == null) {
                        cnx.envoyer("RETRAIT NO non connecte");
                        break;
                    }
                    if (t.length != 2) {
                        cnx.envoyer("RETRAIT NO format incorrect");
                        break;
                    }
                    try {
                        double montant = Double.parseDouble(t[1]);
                        CompteBancaire compte = banque.getCompte(cnx.getNumeroCompteActuel());
                        if (compte != null) {
                            if (compte.debiter(montant)) {
                                cnx.envoyer("RETRAIT OK " + montant);
                            } else {
                                cnx.envoyer("RETRAIT NO solde insuffisant");
                            }
                        } else {
                            cnx.envoyer("RETRAIT NO compte inexistant");
                        }
                    } catch (NumberFormatException e) {
                        cnx.envoyer("RETRAIT NO montant invalide");
                    }
                    break;

                    //6.3 - FACTURE

                case "FACTURE":
                    if (cnx.getNumeroCompteClient() == null) {
                        cnx.envoyer("FACTURE NO non connecte");
                        break;
                    }
                    if (t.length != 4) {
                        cnx.envoyer("FACTURE NO format incorrect");
                        break;
                    }
                    try {
                        double montant = Double.parseDouble(t[1]);
                        String numFacture = t[2];
                        String description = t[3];
                        CompteBancaire compte = banque.getCompte(cnx.getNumeroCompteActuel());
                        if (compte != null) {
                            if (compte.debiter(montant)) {
                                cnx.envoyer("FACTURE OK " + montant + " " + numFacture);
                            } else {
                                cnx.envoyer("FACTURE NO solde insuffisant");
                            }
                        } else {
                            cnx.envoyer("FACTURE NO compte inexistant");
                        }
                    } catch (NumberFormatException e) {
                        cnx.envoyer("FACTURE NO montant invalide");
                    }
                    break;

                    // 6.4 - TRANSFER

                case "TRANSFER":
                    if (cnx.getNumeroCompteClient() == null) {
                        cnx.envoyer("TRANSFER NO non connecte");
                        break;
                    }
                    if (t.length != 3) {
                        cnx.envoyer("TRANSFER NO format incorrect");
                        break;
                    }
                    try {
                        double montant = Double.parseDouble(t[1]);
                        String compteDestinataire = t[2];
                        CompteBancaire compteSource = banque.getCompte(cnx.getNumeroCompteActuel());
                        CompteBancaire compteDest = banque.getCompte(compteDestinataire);

                        if (compteSource == null) {
                            cnx.envoyer("TRANSFER NO compte source inexistant");
                            break;
                        }
                        if (compteDest == null) {
                            cnx.envoyer("TRANSFER NO compte destinataire inexistant");
                            break;
                        }

                        if (compteSource.debiter(montant)) {
                            compteDest.crediter(montant);
                            cnx.envoyer("TRANSFER OK " + montant + " " + compteDestinataire);
                        } else {
                            cnx.envoyer("TRANSFER NO solde insuffisant");
                        }
                    } catch (NumberFormatException e) {
                        cnx.envoyer("TRANSFER NO montant invalide");
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