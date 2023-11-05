package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import src.TransportTypes;
import src.Dns;
import src.Dhcp;

public class Udp {
    private int portSrc; // header IPv4 de 2 octets,il donne la taille du paquets en octets
    private int portDest; // 2 octets, il donne l'id des paquets fragmenté ou jsp quoi
    private int length; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
    
    // Constructeur par défaut sans param sauf le nb d'octets lu jusqu'à maintenant
    public Udp(){
        this.portSrc = -1;
        this.portDest = -1;
        this.length = -1;
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public void readUdp(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        
        System.out.println("\n----------- UDP segment -----------");
        // portSrc 
        byte [] portSrcBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(portSrcBuffer);
        
        // Pour le moment on saute cette étape pour convertir tous le bloc et pas juste byte par byte
        /*StringBuilder portSrcSB = new StringBuilder();
        //System.out.printf("%d", portSrcBuffer);
        for (byte b : portSrcBuffer)
            //System.out.printf("%d", b & 0xFF);
            portSrcSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal

        // On doit convertir tous le bloc hexa et non pas 2 hexa par 2 hexa (car on peut treouver des port à 65000)
        this.portSrc = con(Integer.valueOf(portSrcSB.toString()));*/
        
        this.portSrc = convertirEnDecimal(portSrcBuffer);
        System.out.println("> Source Port: " + this.portSrc);
        // portDest 
        byte [] portDestBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(portDestBuffer);
        /*StringBuilder portDestSB = new StringBuilder();
        for (byte b : portDestBuffer)
            portDestSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.portDest = Integer.valueOf(portDestSB.toString());*/
        this.portDest = convertirEnDecimal(portDestBuffer);
        System.out.println("> Destination Port: " + this.portDest);
        // Length 
        byte [] lengthBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(lengthBuffer);
        StringBuilder lengthSB = new StringBuilder();
        for (byte b : lengthBuffer)
            lengthSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.length = Integer.valueOf(lengthSB.toString());
        System.out.println("> Length: " + this.length);

        // On saute: checksum (2octets)
        packetByteCount += (int)dataInputStream.skip(2);
        
        // Lecture des protocoles applicatifs
        if ((this.portDest == TransportTypes.DNS) || (this.portSrc == TransportTypes.DNS)){
            Dns dnsRecord = new Dns();
            dnsRecord.readDns(dataInputStream, packetLength, packetByteCount);
        }
        else if((this.portDest == TransportTypes.HTTP) || (this.portSrc == TransportTypes.HTTP)){

        }
        else if((this.portDest == TransportTypes.HTTPS) || (this.portSrc == TransportTypes.HTTPS)){

        }
        else if ((this.portDest == TransportTypes.DHCP) || (this.portSrc == TransportTypes.DHCP)){
            // !!! ACHANGER !!! --> Voir comment skipper à pplus haut niveau pour bien controler la totalité des octets lues
            Dhcp dhcp = new Dhcp();
            packetByteCount = dhcp.readDhcp(dataInputStream, packetLength, packetByteCount);
            //dataInputStream.skip(packetLength-packetByteCount); // Si 
        }
        // Pour lire la suite on part du principe que y'a pas d'options IPV4 (en réalité y'a que des options IPv4 si l'IHL est > 5)
        // En vrai go faire un if IHL < 5 faire ça et sinon mettre un msg pour dire que c'est pas supporté
        else{
            //System.out.println("dest port n°" + portDest + ", " + "src port n°" + portSrc);
            dataInputStream.skip(packetLength-packetByteCount);
        }
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
}
