package src;

import java.io.DataInputStream;
import java.io.IOException;

public class DnsQuestion {

    private int labelSize; //à 0 on sort du while car on a fini de lire la Question section du record DNS
    private String label;
    private String qName;
    private String qType;
    private String qClass;

    public DnsQuestion(){
        this.labelSize = -1;
        this.label = ""; // Représente le query Name (QNAME)
        this.qType = "";
        this.qClass = "";
    }

    public int readQuestionSection(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        
        // QNAME
        while (labelSize != 0){
            byte [] labelSizeBuffer = new byte[1];
            packetByteCount += (int)dataInputStream.read(labelSizeBuffer);
            StringBuilder labelSizeSB = new StringBuilder();
            for (byte b : labelSizeBuffer)
                labelSizeSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal

            this.labelSize = Integer.valueOf(labelSizeSB.toString());           
            if (this.labelSize != 0){
                // Lire le nombre d'octet du label (grace à labelSize et convertir chaque octet en ASCII)
                byte [] labelBuffer = new byte[this.labelSize];
                packetByteCount += (int)dataInputStream.read(labelBuffer);
                StringBuilder labelSB = new StringBuilder();
                for (byte b : labelBuffer)
                    labelSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
                
                this.label += convertirHexEnTexte(labelSB.toString()); // On convertit en texte les valeurs hexa lu dans ce label
                this.label+= ".";
            }
        }
        // 
        this.label = label.substring(0, this.label.length() - 1);//Supprime le dernier point rajouter en trop
        System.out.println("Query Name: " + this.label);

        // QType
        byte [] qTypeBuffer = new byte[2];
        packetByteCount += (int)dataInputStream.read(qTypeBuffer);
        StringBuilder qTypeSB = new StringBuilder();
        for (byte b : qTypeBuffer)
            qTypeSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.qType = qTypeSB.toString();
        System.out.println("type: " + this.qType);

        
        // QClass
        byte [] qClassBuffer = new byte[2];
        packetByteCount += (int)dataInputStream.read(qClassBuffer);
        StringBuilder qClassSB = new StringBuilder();
        for (byte b : qClassBuffer)
            qClassSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.qClass = qClassSB.toString();
        System.out.println("class: " + this.qClass);


        return packetByteCount; // On renvoit sa valeur pour mettre à jour le nombre de d'octet à lire plus haut (dans le DNS)
    }

    public static String convertirHexEnTexte(String hex) {
        StringBuilder texte = new StringBuilder();

        for (int i = 0; i < hex.length(); i += 2) {
            // Paire de caractères hexadécimaux
            String paireHex = hex.substring(i, i + 2);
    
            // Convertir chaque paire de caractères hexadécimaux en un caractère
            char caractere = (char) Integer.parseInt(paireHex, 16);// Base 16
    
            // Ajout du caractère à la chaîne de texte
            texte.append(caractere);
        }
    
        return texte.toString();
    }
}