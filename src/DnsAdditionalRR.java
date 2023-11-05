package src;

import java.io.DataInputStream;
import java.io.IOException;

public class DnsAdditionalRR {
    private String name;
    private String type;

    public DnsAdditionalRR(){
        this.name = ""; // 1 o
        this.type = ""; // 2 o
    }

    public int readAdditionalRecordSection(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        System.out.println("<Additional Record>");
        // Name
        byte [] nameBuffer = new byte[1];
        packetByteCount += (int)dataInputStream.read(nameBuffer);
        StringBuilder nameSB = new StringBuilder();
        for (byte b : nameBuffer)
            nameSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.name = nameSB.toString();
        System.out.println("name: " + this.name);
        // type
        byte [] typeBuffer = new byte[2];
        packetByteCount += (int)dataInputStream.read(typeBuffer);
        StringBuilder typeSB = new StringBuilder();
        for (byte b : typeBuffer)
            typeSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.type = typeSB.toString();
        System.out.println("type: " + this.type);

        return packetByteCount;
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