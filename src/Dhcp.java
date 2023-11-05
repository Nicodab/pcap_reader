package src;

import java.io.DataInputStream;
import java.io.IOException;

public class Dhcp {

    private int messageType; // header IPv4 de 2 octets,il donne la taille du paquets en octets
    private int hardwareType; // 2 octets, il donne l'id des paquets fragmenté ou jsp quoi
    private int hardwareLength; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
    private int hops;
    private String xid;
    private int secondElapsed;
    //private int []flags; // [2]=URG [3]=ACK [4]=PSH [5]=RST [6]=SYN [7]=FIN
    private String clientAddress;
    private String yourClientAdress;
    private String nextServerAddress;
    private String relayAgentAddress;

    // Constructeur par défaut sans param sauf le nb d'octets lu jusqu'à maintenant
    public Dhcp(){
        this.messageType = -1;
        this.hardwareType = -1;
        this.hardwareLength = -1;
        this.hops = -1;
        this.xid = ""; //Tableau de 'bit' représentant un octet
        this.secondElapsed = -1;
        this.clientAddress = "";
        this.yourClientAdress = "";
        this.nextServerAddress = "";
        this.relayAgentAddress = "";
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public int readDhcp(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        //int bytesCountTcp = 0;
        System.out.println("\n----------- DHCP protocol -----------");
        // Message Type 
        byte [] messageTypeBuffer = new byte[1]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(messageTypeBuffer);
        this.messageType = convertirEnDecimal(messageTypeBuffer);
        System.out.println("> Message Type: " + this.messageType);

        // Hardware Type 
        byte [] hardwareTypeBuffer = new byte[1]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(hardwareTypeBuffer);
        this.hardwareType = convertirEnDecimal(hardwareTypeBuffer);
        System.out.println("> Hardware Type: " + this.hardwareType);

        // Hardware Length 
        byte [] hardwareLengthBuffer = new byte[1]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(hardwareLengthBuffer);
        this.hardwareLength = convertirEnDecimal(hardwareLengthBuffer);
        System.out.println("> Hardware Length: " + this.hardwareLength);

        // hops 
        byte [] hopsBuffer = new byte[1]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(hopsBuffer);
        this.hops = convertirEnDecimal(hopsBuffer);
        System.out.println("> Hops: " + this.hops);

        // portSrc 
        byte [] xidBuffer = new byte[4]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(xidBuffer);
        StringBuilder xidSB = new StringBuilder();
        // Affichez les octets inversés en décimal
        for (byte b : xidBuffer) {
            xidSB.append(String.format("%02X", b & 0xFF));
        }
        this.xid = xidSB.toString(); // affectation à l'attribut de la classe 
        System.out.println("> Transaction ID: " + this.xid);

        // portSrc 
        byte [] secondElapsedBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(secondElapsedBuffer);
        this.secondElapsed = convertirEnDecimal(secondElapsedBuffer);
        System.out.println("> Second Elapsed: " + this.secondElapsed);

        // On saute les flags (2 octets)
        packetByteCount += (int)dataInputStream.skip(packetByteCount);

        // Les adresse IP /////////////////
        int compteurIp = 0;
        // clientAddress
        byte [] clientAddressBuffer = new byte[4];
        packetByteCount += (int)dataInputStream.read(clientAddressBuffer);
        StringBuilder clientAddressSB = new StringBuilder();
        for (byte b : clientAddressBuffer){
            clientAddressSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp += 1;
            if (compteurIp != clientAddressBuffer.length) clientAddressSB.append(".");
        }
        compteurIp = 0;
        this.clientAddress = clientAddressSB.toString();
        System.out.println("> Client IP Address: " + this.clientAddress);

        // yourClientAdress
        byte [] yourClientAdressBuffer = new byte[4]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(yourClientAdressBuffer);
        StringBuilder yourClientAdressSB = new StringBuilder();
        for (byte b : yourClientAdressBuffer){
            yourClientAdressSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp += 1;
            if (compteurIp != yourClientAdressBuffer.length) yourClientAdressSB.append(".");
        }
            
        compteurIp = 0;
        this.yourClientAdress = yourClientAdressSB.toString();
        System.out.println("> 'Your' Client IP Adress: " + this.yourClientAdress);

        // nextServerAddress
        byte [] nextServerAddressBuffer = new byte[4]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(nextServerAddressBuffer);
        StringBuilder nextServerAddressSB = new StringBuilder();
        for (byte b : nextServerAddressBuffer){
            nextServerAddressSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp += 1;
            if (compteurIp != nextServerAddressBuffer.length) nextServerAddressSB.append(".");
        }
            
        compteurIp = 0;
        this.nextServerAddress = nextServerAddress.toString();
        System.out.println("> Next Server IP Adress: " + this.yourClientAdress);

        // relayAgentAddress
        byte [] relayAgentAddressBuffer = new byte[4]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(relayAgentAddressBuffer);
        StringBuilder relayAgentAddressSB = new StringBuilder();
        for (byte b : relayAgentAddressBuffer){
            relayAgentAddressSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp += 1;
            if (compteurIp != relayAgentAddressBuffer.length) relayAgentAddressSB.append(".");
        }
            
        compteurIp = 0;
        this.relayAgentAddress = relayAgentAddress.toString();
        if (this.relayAgentAddress.isEmpty()) System.out.println("Relay Agent IP Adress: None");
        else System.out.println("> Relay Agent IP Adress: " + this.relayAgentAddress);

        // Pour l'instant on skippe le reste ici
        // sauf les options de 312 octets potentiellemnt carc'est variable
        int skipRest = 16 + 64 + 128;
        packetByteCount += dataInputStream.skip(packetLength-packetByteCount);

        return packetByteCount;
    }

    // Retourne la valeur décimal d'une séquence/bloc d'octets
    public static int convertirEnDecimal(byte[] byteArray) {
        int decimalValue = 0; // Initialiser la valeur décimale à zéro
        for (int i = 0; i < byteArray.length; i++) {
            int octet = byteArray[i] & 0xFF; // Utiliser un masque pour s'assurer que l'octet est interprété comme non signé
            decimalValue += octet << (8 * (byteArray.length - 1 - i)); // Effectuer un décalage en fonction de la position de l'octet
        }
        return decimalValue;
    }

    // Retourne la valeur décimal d'une séquence/bloc d'octets
    public static long convertirEnDecimalLong(byte[] byteArray) {
        long decimalValue = 0; // Initialiser la valeur décimale à zéro
        for (int i = 0; i < byteArray.length; i++) {
            int octet = byteArray[i] & 0xFF; // Utiliser un masque pour s'assurer que l'octet est interprété comme non signé
            decimalValue += octet << (8 * (byteArray.length - 1 - i)); // Effectuer un décalage en fonction de la position de l'octet
        }
        return decimalValue;
    }

}
