package com.atoudeft.banque;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Banque implements Serializable {
    private String nom;
    private List<CompteClient> comptes;

    public Banque(String nom) {
        this.nom = nom;
        this.comptes = new ArrayList<>();
    }

    /**
     * Recherche un compte-client à partir de son numéro.
     *
     * @param numeroCompteClient le numéro du compte-client
     * @return le compte-client s'il a été trouvé. Sinon, retourne null
     */
    public CompteClient getCompteClient(String numeroCompteClient) {
        CompteClient cpt = new CompteClient(numeroCompteClient,"");
        int index = this.comptes.indexOf(cpt);
        if (index != -1)
            return this.comptes.get(index);
        else
            return null;
    }

    /**
     * Vérifier qu'un compte-bancaire appartient bien au compte-client.
     *
     * @param numeroCompteBancaire numéro du compte-bancaire
     * @param numeroCompteClient    numéro du compte-client
     * @return  true si le compte-bancaire appartient au compte-client
     */
    public boolean appartientA(String numeroCompteBancaire, String numeroCompteClient) {
        throw new NotImplementedException();
    }

    /**
     * Effectue un dépot d'argent dans un compte-bancaire
     *
     * @param montant montant à déposer
     * @param numeroCompte numéro du compte
     * @return true si le dépot s'est effectué correctement
     */
    public boolean deposer(double montant, String numeroCompte) {
        throw new NotImplementedException();
    }

    /**
     * Effectue un retrait d'argent d'un compte-bancaire
     *
     * @param montant montant retiré
     * @param numeroCompte numéro du compte
     * @return true si le retrait s'est effectué correctement
     */
    public boolean retirer(double montant, String numeroCompte) {
        throw new NotImplementedException();
    }

    /**
     * Effectue un transfert d'argent d'un compte à un autre de la même banque
     * @param montant montant à transférer
     * @param numeroCompteInitial   numéro du compte d'où sera prélevé l'argent
     * @param numeroCompteFinal numéro du compte où sera déposé l'argent
     * @return true si l'opération s'est déroulée correctement
     */
    public boolean transferer(double montant, String numeroCompteInitial, String numeroCompteFinal) {
        throw new NotImplementedException();
    }

    /**
     * Effectue un paiement de facture.
     * @param montant montant de la facture
     * @param numeroCompte numéro du compte bancaire d'où va se faire le paiement
     * @param numeroFacture numéro de la facture
     * @param description texte descriptif de la facture
     * @return true si le paiement s'est bien effectuée
     */
    public boolean payerFacture(double montant, String numeroCompte, String numeroFacture, String description) {
    //    throw new NotImplementedException();
    return false;
    }

    /**
     * Crée un nouveau compte-client avec un numéro et un nip et l'ajoute à la liste des comptes.
     *
     * @param numCompteClient numéro du compte-client à créer
     * @param nip nip du compte-client à créer
     * @return true si le compte a été créé correctement
     */



    public boolean ajouter(String numCompteClient, String nip) {
        /*À compléter et modifier :
            - Vérifier que le numéro a entre 6 et 8 caractères et ne contient que des lettres majuscules et des chiffres.
              Sinon, retourner false.
            - Vérifier que le nip a entre 4 et 5 caractères et ne contient que des chiffres. Sinon,
              retourner false.
            - Vérifier s'il y a déjà un compte-client avec le numéro, retourner false.
            - Sinon :
                . Créer un compte-client avec le numéro et le nip;
                . Générer (avec CompteBancaire.genereNouveauNumero()) un nouveau numéro de compte bancaire qui n'est
                  pas déjà utilisé;
                . Créer un compte-chèque avec ce numéro et l'ajouter au compte-client;
                . Ajouter le compte-client à la liste des comptes et retourner true.
         */

        // Vérifier si le numéro de compte client est valide (6-8 caractères, lettres majuscules et chiffres)

        //return this.comptes.add(new CompteClient(numCompteClient,nip)); //À modifier**

        if (numCompteClient.length() < 6 || numCompteClient.length() > 8 || !numCompteClient.matches("[A-Z0-9]+")) {
            return false;
        }

        if (nip.length() < 4 || nip.length() > 5 || !nip.matches("[0-9]+")) {
            return false;
        }

        if (comptes.equals(numCompteClient)) {
            return false;
        }

        //creation nouveau compte client
        CompteClient compteClient = new CompteClient(numCompteClient, nip);

        // Génération numéro compte bancaire unique
        final String numCompteBancaire = CompteBancaire.genereNouveauNumero();
        if (compteBancaireExiste(numCompteBancaire)) {
            return false;  // Si le numéro existe déjà, on abandonne la création
        }

        CompteBancaire compteCheque = new CompteBancaire(numCompteBancaire, TypeCompte.CHEQUE) {
            private double solde = 0.0;

            @Override
            public boolean crediter(double montant) {
                if (montant > 0) {
                    solde += montant;
                    return true;
                }
                return false;
            }

            @Override
            public boolean debiter(double montant) {
                if (montant > 0 && montant <= solde) {
                    solde -= montant;
                    return true;
                }
                return false;
            }

            @Override
            public double getSolde() {
                return solde;
            }

            @Override
            public String getNumero() {
                return numCompteBancaire;
            }

            @Override
            public boolean payerFacture(String description, double montant, String numeroFacture) {
                return debiter(montant);
            }

            @Override
            public boolean transferer(double montant, String numeroCompteDestination) {
                if (debiter(montant)) {
                    // Note: In a real implementation, we'd need to handle the crediting of the destination account
                    // through the bank's transferer method
                    return true;
                }
                return false;
            }
        };

        // Ajouter le compte chèque au compte client
        compteClient.ajouter(compteCheque);

        // Ajouter le compte client à la liste des comptes
        comptes.add(compteClient);

        return true;
    }


    /**
     * Retourne le numéro du compte-chèque d'un client à partir de son numéro de compte-client.
     *
     * @param numCompteClient numéro de compte-client
     * @return numéro du compte-chèque du client ayant le numéro de compte-client
     */
    public String getNumeroCompteParDefaut(String numCompteClient) {
        //À compléter : retourner le numéro du compte-chèque du compte-client.
        return null; //À modifier
    }

    public boolean compteBancaireExiste(String numeroCompteBancaire) {
        for (CompteClient compteClient : comptes) {
            for (CompteBancaire compteBancaire : compteClient.getComptes()) {
                if (compteBancaire.getNumero().equals(numeroCompteBancaire)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Vérifie si le NIP correspond au compte client donné
     */
    public boolean verifierNip(String numeroCompteClient, String nip) {
        CompteClient client = getCompteClient(numeroCompteClient);
        if (client != null) {
            return client.verifierNip(nip);
        }
        return false;
    }

    /**
     * Vérifie si le client possède déjà un compte épargne
     */
    public boolean clientPossedeCompteEpargne(String numeroCompteClient) {
        CompteClient client = getCompteClient(numeroCompteClient);
        if (client != null) {
            for (CompteBancaire compte : client.getComptes()) {
                if (compte instanceof CompteEpargne) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si un numéro de compte existe déjà
     */
    public boolean numeroCompteExistant(String numeroCompte) {
        for (CompteClient client : comptes) {
            for (CompteBancaire compte : client.getComptes()) {
                if (compte.getNumero().equals(numeroCompte)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ajoute un compte bancaire à un client existant
     */
    public boolean ajouter(String numeroCompteClient, CompteBancaire compte) {
        CompteClient client = getCompteClient(numeroCompteClient);
        if (client != null) {
            return client.ajouter(compte);
        }
        return false;
    }

    /**
     * Retourne le numéro du compte épargne d'un client
     */
    public String getNumeroCompteEpargne(String numeroCompteClient) {
        CompteClient client = getCompteClient(numeroCompteClient);
        if (client != null) {
            for (CompteBancaire compte : client.getComptes()) {
                if (compte instanceof CompteEpargne) {
                    return compte.getNumero();
                }
            }
        }
        return null;
    }

    /**
     * Crédite un montant sur un compte
     */

    public CompteBancaire getCompte(String numeroCompte) {
        for (CompteClient client : comptes) {
            for (CompteBancaire compte : client.getComptes()) {
                if (compte.getNumero().equals(numeroCompte)) {
                    return compte;
                }
            }
        }
        return null; // Compte not found
    }
}