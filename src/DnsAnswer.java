package src;

import java.io.DataInputStream;
import java.io.IOException;


public class DnsAnswer {
    private int nameSize; //à 0 on sort du while car on a fini de lire la Question section du record DNS
    private String name;
    private String type;
    private String _class;
    private int ttl;
    private int dataLength;
    private String data;
    
    //private String _class;

    public DnsAnswer(){
        this.nameSize = -1;
        this.name = ""; // Représente le query Name (QNAME)
        this.type = "";
        this._class = "";
        this.ttl = -1;
        this.dataLength = -1;
        this.data = "";
    }

    public int readAnswer(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        System.out.println("<Anwser>");
        // NAME
        packetByteCount += dataInputStream.skip(2); // On skippe 2 bit ici mais on devrait pas du tout!à revoir comment bien le mettre en place
        /*while (nameSize != 0){
            byte [] nameSizeBuffer = new byte[1];
            packetByteCount += (int)dataInputStream.read(nameSizeBuffer);
            StringBuilder nameSizeSB = new StringBuilder();
            StringBuilder testNameSB = new StringBuilder();
            for (byte b : nameSizeBuffer){
                //nameSizeSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
                testNameSB.append(b & 0xFF);
            }
            System.out.println(testNameSB.toString());
            System.exit(0);
            
            this.nameSize = Integer.valueOf(nameSizeSB.toString());
            System.out.println("nameSize str: " + nameSizeSB.toString());
            System.out.println("nameSize: " + nameSize);
            if (this.nameSize > 0){
                System.out.println("C'est > à 0");
                // Lire le nombre d'octet du name (grace à nameSize et convertir chaque octet en ASCII)
                byte [] nameBuffer = new byte[this.nameSize];
                packetByteCount += (int)dataInputStream.read(nameBuffer);
                StringBuilder nameSB = new StringBuilder();
                for (byte b : nameBuffer)
                    nameSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
                
                this.name += convertirHexEnTexte(nameSB.toString()); // On convertit en texte les valeurs hexa lu dans ce name
                this.name+= ".";
            }
        }
        
        this.name = name.substring(0, this.name.length() - 1);//Supprime le dernier point rajouter en trop
        System.out.println("Name: " + this.name);*/


        // Type
        byte [] typeBuffer = new byte[2];
        packetByteCount += (int)dataInputStream.read(typeBuffer);
        StringBuilder typeSB = new StringBuilder();
        for (byte b : typeBuffer)
            typeSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.type = typeSB.toString();
        System.out.println("type: " + this.type);
        
        // class
        byte [] _classBuffer = new byte[2];
        packetByteCount += (int)dataInputStream.read(_classBuffer);
        StringBuilder _classSB = new StringBuilder();
        for (byte b : _classBuffer)
            _classSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this._class = _classSB.toString();
        System.out.println("class: " + this._class);

        // ttl
        byte [] ttlBuffer = new byte[4];
        packetByteCount += (int)dataInputStream.read(ttlBuffer);
        StringBuilder ttlSB = new StringBuilder();
        for (byte b : ttlBuffer)
            ttlSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.ttl = Integer.parseInt(ttlSB.toString(), 16);
        System.out.println("ttl: " + this.ttl);

        // data length
        byte [] dataLengthBuffer = new byte[2];
        packetByteCount += (int)dataInputStream.read(dataLengthBuffer);
        StringBuilder dataLengthSB = new StringBuilder();
        for (byte b : dataLengthBuffer)
            dataLengthSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.dataLength = Integer.valueOf(dataLengthSB.toString());
        System.out.println("Data Length: " + this.dataLength);

        if (this.dataLength > -1){
            int compteur = 0;
            // data
            byte [] dataBuffer = new byte[this.dataLength];
            packetByteCount += (int)dataInputStream.read(dataBuffer);
            StringBuilder dataSB = new StringBuilder();
            for (byte b : dataBuffer){
                dataSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
                compteur += 1;
                if (compteur != dataBuffer.length) dataSB.append(".");
            }
            this.data = dataSB.toString();
            System.out.println("Data: " + this.data);
        }

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