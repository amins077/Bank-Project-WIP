package com.atoudeft.banque;

import com.sun.org.apache.xpath.internal.operations.Operation;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class CompteBancaire implements Serializable {
    private String numero;
    private TypeCompte type;
    private double solde;

    /**
     * Génère un numéro de compte bancaire aléatoirement avec le format CCC00C, où C est un caractère alphabétique
     * majuscule et 0 est un chiffre entre 0 et 9.
     *
     * @return
     */
    public static String genereNouveauNumero() {
        char[] t = new char[6];
        for (int i = 0; i < 3; i++) {
            t[i] = (char) ((int) (Math.random() * 26) + 'A');
        }
        for (int i = 3; i < 5; i++) {
            t[i] = (char) ((int) (Math.random() * 10) + '0');
        }
        t[5] = (char) ((int) (Math.random() * 26) + 'A');
        return new String(t);
    }

    /**
     * Crée un compte bancaire.
     *
     * @param numero numéro du compte
     * @param type   type du compte
     */
    public CompteBancaire(String numero, TypeCompte type) {
        this.numero = numero;
        this.type = type;
        this.solde = 0;
    }

    public String getNumero() {
        return numero;
    }

    public TypeCompte getType() {
        return type;
    }

    public double getSolde() {
        return solde;
    }

    public abstract boolean crediter(double montant);

    public abstract boolean debiter(double montant);

    public abstract boolean payerFacture(String numeroFacture, double montant, String description);

    public abstract boolean transferer(double montant, String numeroCompteDestinataire);

    protected void setSolde(double nouveauSolde) {
        this.solde = nouveauSolde;
    }

    //7.1

    public enum TypeOperation {
        DEPOT,
        RETRAIT,
        TRANSFER,
        FACTURE
    }

    public abstract class Operation {

        private CompteBancaire.TypeOperation type;
        private Date dateOperation;

        //Constructeur pour initialiser le type et la date de l'opération
        public Operation(CompteBancaire.TypeOperation type) {
            this.type = type;
            this.dateOperation = new Date(System.currentTimeMillis());
        }

        //Getter pour le type de l'opération
        public CompteBancaire.TypeOperation getType() {
            return type;
        }

        //Getter pour la date de l'opération
        public Date getDateOperation() {
            return dateOperation;
        }

        public abstract void executer();
    }

    //7.2

    public class OperationDepot extends Operation {

        private double montant;

        public OperationDepot(double montant) {
            super(TypeOperation.DEPOT);
            this.montant = montant;
        }

        public double getMontant() {
            return  montant;
        }

        @Override
        public void executer() {
            //Logique pour éxecuter l'opération de dépot
            System.out.println("Dépôt de " + montant + " unités.");
        }

        //7.3
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(getDateOperation());
            return "DATE: " + dateStr + "\nTYPE: " + getType() + "\nMONTANT: " + montant;
        }
    }

    public class OperationRetrait extends Operation {

        private double montant;

        public OperationRetrait(double montant) {
            super(TypeOperation.RETRAIT);
            this.montant = montant;
        }

        public double getMontant() {
            return montant;
        }

        @Override
        public void executer() {
            //Logique pour éxecuter l'opération de retrait
            System.out.println("Retrait de " + montant + " unités.");
        }

        //7.3
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(getDateOperation());
            return "DATE: " + dateStr + "\nTYPE: " + getType() + "\nMONTANT: " + montant;
        }
    }

    public class OperationTransfer extends Operation {

        private double montant;

        public OperationTransfer(double montant) {
            super(TypeOperation.TRANSFER);
            this.montant = montant;
        }

        public double getMontant() {
            return montant;
        }

        @Override
        public void executer() {
            //Logique pour éxecuter l'opération de transfer.
            System.out.println("Transfer de " + montant + " unités");
        }

        //7.3
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(getDateOperation());
            return "DATE: " + dateStr + "\nTYPE: " + getType() + "\nMONTANT: " + montant;
        }
    }

    public class OperationFacture extends Operation {

        private double montant;
        private String numeroFacture;
        private String description;

        public OperationFacture(double montant, String numeroFacture, String description) {
            super(TypeOperation.FACTURE);
            this.montant = montant;
            this.numeroFacture = numeroFacture;
            this.description = description;
        }

        public double getMontant() {
            return montant;
        }

        public String getNumeroFacture() {
            return numeroFacture;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public void executer() {
            //Logique pour éxecuter l'opération de facture.
            System.out.println("Paiement de la facture " + numeroFacture + " de " + montant + " unités pour " + description + ".");
        }

        //7.3
        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(getDateOperation());
            return "DATE: " + dateStr + "\nTYPE: " + getType() + "\nMONTANT: " + montant + "\nNUMÉRO DE FACTURE: " + numeroFacture + "\nDESCRIPTION: " + description;
        }
    }
}