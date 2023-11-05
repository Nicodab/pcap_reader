package src;

import java.io.DataInputStream;
import java.io.IOException;

public class Icmp {
    private int messageType; // demande écho, 
    private int errorCode; // code 1, 2,3 ou parfois plus, dépendant du type de msg. POar exemple pour un ping y'a que le code 0 qui existe 
    private String checksum; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
    // A voir comment faire avec le payload
    
    public Icmp(){
        this.messageType = -1;
        this.errorCode = -1;
        this.checksum = "";
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public int readIcmp(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        //int bytesCountTcp = 0;
        System.out.println("----------- ICMP protocol -----------");
        // Message Type 
        byte [] messageTypeBuffer = new byte[1]; // message type: 2 octets
        packetByteCount += (int)dataInputStream.read(messageTypeBuffer);
        this.messageType = convertirEnDecimal(messageTypeBuffer);
        System.out.println("Message Type: " + this.messageType);

        // Hardware Type 
        byte [] errorCodeBuffer = new byte[1]; // Code: 1 octets
        packetByteCount += (int)dataInputStream.read(errorCodeBuffer);
        this.errorCode = convertirEnDecimal(errorCodeBuffer);
        System.out.println("Code: " + this.errorCode);

        // Hardware Length 
        byte [] checksumBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(checksumBuffer);
        StringBuilder checksumSB = new StringBuilder();
        // Affichez les octets inversés en décimal
        for (byte b : checksumBuffer) {
           // System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            checksumSB.append(String.format("%02X", b & 0xFF));
        }
        this.checksum = checksumSB.toString(); // affectation à l'attribut de la classe 
        System.out.println("Checksum: " + this.checksum);
        packetByteCount += dataInputStream.skip(packetLength-packetByteCount);
        return packetByteCount;
    }
    // Retourne la valeur décimak d'une séquence/bloc d'octets
    public static int convertirEnDecimal(byte[] byteArray) {
        int decimalValue = 0; // Initialiser la valeur décimale à zéro
        for (int i = 0; i < byteArray.length; i++) {
            int octet = byteArray[i] & 0xFF; // Utiliser un masque pour s'assurer que l'octet est interprété comme non signé
            decimalValue += octet << (8 * (byteArray.length - 1 - i)); // Effectuer un décalage en fonction de la position de l'octet
        }
        return decimalValue;
    }

    // Retourne la valeur décimak d'une séquence/bloc d'octets
    public static long convertirEnDecimalLong(byte[] byteArray) {
        long decimalValue = 0; // Initialiser la valeur décimale à zéro
        for (int i = 0; i < byteArray.length; i++) {
            int octet = byteArray[i] & 0xFF; // Utiliser un masque pour s'assurer que l'octet est interprété comme non signé
            decimalValue += octet << (8 * (byteArray.length - 1 - i)); // Effectuer un décalage en fonction de la position de l'octet
        }
        return decimalValue;
    }   
}
