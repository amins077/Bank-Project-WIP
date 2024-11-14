package com.atoudeft.banque;

public class CompteCheque extends CompteBancaire {

    public CompteCheque(String nom, TypeCompte typeCompte) {
        super(nom, typeCompte); // Appelle le constructeur de la classe parente
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
            setSolde(getSolde() - montant);
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
}