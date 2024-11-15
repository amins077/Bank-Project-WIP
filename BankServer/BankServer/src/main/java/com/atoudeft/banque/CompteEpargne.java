package com.atoudeft.banque;

public class CompteEpargne extends CompteBancaire {

    private static final double LIMITE_SOLDE = 1000;
    private static final double FRAIS_DEBIT = 2.0;
    private double tauxInterets;

    public CompteEpargne(String nom, TypeCompte typeCompte, double tauxInterets) {
        super(nom, typeCompte); // Appelle le constructeur de la classe parente
        this.tauxInterets = tauxInterets;  //Initialise taux interets
    }

    @Override
    public boolean crediter(double montant) {
        if (montant > 0) {
            setSolde(getSolde() + montant);
            return true;
        }
        return false;
    }

    @Override
    public boolean debiter(double montant) {
        if (montant > 0 && montant <= getSolde()) {
            double nouveauSolde = getSolde() - montant;
            setSolde(nouveauSolde);
            if (nouveauSolde < LIMITE_SOLDE) {
                setSolde(getSolde() - FRAIS_DEBIT);  //applique frais
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean payerFacture(String numeroFacture, double montant, String description) {
        return false;
    }

    @Override
    public boolean transferer(double montant, String numeroCompteDestinataire) {
        return false;
    }

    public void ajouterInterets() {
        double interets = getSolde() + tauxInterets / 100;
        setSolde(getSolde() + interets);
    }

    public double getTauxInterets() {
        return tauxInterets;
    }

    public void setTauxInterets() {
        this.tauxInterets = tauxInterets;
    }
}
