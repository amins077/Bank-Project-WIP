package com.atoudeft.banque;

public class CompteCheque extends CompteBancaire {

        public CompteCheque() {
            super(); // Appelle le constructeur de la classe parente
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
            if (montant > 0 && getSolde() >= montant) {
                setSolde(getSolde() - montant);
                return true;
            }
            return false;
        }

        @Override
        public boolean payerFacture(double montant) {
            return false; // Pour le moment, retourne toujours false
        }

        @Override
        public boolean transferer(CompteBancaire compte, double montant) {
            return false; // Pour le moment, retourne toujours false
        }
    }
