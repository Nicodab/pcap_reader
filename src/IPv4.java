package src;

import java.io.DataInputStream;
import java.io.IOException;
import src.IPv4Types;
import src.Udp;
import src.Tcp;
import src.Http;
import src.Icmp;

public class IPv4 {
    private int totalLength; // header IPv4 de 2 octets,il donne la taille du paquets en octets
    private int totalLengthDecimalValue;
    private String indentification; // 2 octets, il donne l'id des paquets fragmenté ou jsp quoi
    private String protocol; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
    private String ttl; // Time to live: 1 octets. Utilisé pour le nb de hops en général 
    private String ipSrc; // 4 octets
    private String ipDest; // 4 octets
    private String transportLayer; // UDP, TCP ou QUIC
    private int payloadLength;

    private boolean tcpFilter = false;
    private boolean udpFilter = false;
    private boolean icmpFilter = false;

    // Constructeur par défaut sans param sauf le nb d'octets lu jusqu'à maintenant
    public IPv4(){
        this.totalLength = -1;
        this.indentification = "";
        this.protocol = "";
        this.ttl = "";
        this.ipSrc = "";
        this.ipDest = "";
        this.payloadLength = 0;
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public void readIPv4(DataInputStream dataInputStream, int packetLength, int packetByteCount, String filter) throws IOException {
        
        System.out.println("----------- IPv4 Packet -----------");
        // On saute: la version (4 bits), IHL (4 bits), DSCP (6 bits), ECN (2 bits) --> 2 octets en tout
        packetByteCount += (int)dataInputStream.skip(2);
        // Total Length 
        byte [] totalLengthBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(totalLengthBuffer);
        /*StringBuilder totalLengthSB = new StringBuilder();
        StringBuilder totalLengthHexaSB = new StringBuilder();
        for (byte b : totalLengthBuffer){
            totalLengthSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            totalLengthHexaSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        }
        System.out.println("Total Length hexa: " + totalLengthHexaSB.toString());*/
        this.totalLength = convertirEnDecimal(totalLengthBuffer);
        System.out.println("Total Length: " + this.totalLength);

        // Identification
        byte [] identificationBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(identificationBuffer);
        StringBuilder identificationSB = new StringBuilder();
        for (byte b : identificationBuffer)
            identificationSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.indentification = identificationSB.toString();
        System.out.println("Identification: " + this.indentification);

        // On saute: les flags (3 bits) et le Fragment Offset (13 bits) --> 2 octets en tout
        packetByteCount += (int)dataInputStream.skip(2);

        // Time To Live
        byte [] ttlBuffer = new byte[1];
        packetByteCount += (int)dataInputStream.read(ttlBuffer);
        StringBuilder ttlSB = new StringBuilder();
        for (byte b : ttlBuffer)
            ttlSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.ttl = ttlSB.toString();
        System.out.println("Time to Live: " + this.ttl);

        // ProtocolNumber (17=UDP, 6=TCP)
        byte [] protocolBuffer = new byte[1]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(protocolBuffer);
        StringBuilder protocolSB = new StringBuilder();
        for (byte b : protocolBuffer)
            protocolSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        this.protocol = protocolSB.toString();
        System.out.println("Protocol number: " + this.protocol);
        int protocolNumberInteger = -1; // -1 quand c'est pas reconnu/supporté par l'appli
        try{
            protocolNumberInteger = Integer.valueOf(this.protocol);
        }catch (NumberFormatException e){
            System.out.println(e);
            System.exit(1);
        }
        /*System.out.println("< LE FILTRE > : " + filter);

        //Recherche de filtre, si c'est le cas on passe le corresponfdant à true
        if ((protocolNumberInteger == 17) && filter.equals("udp")){
            System.out.println("filter UDP");
            udpFilter = true;
        }
        if ((protocolNumberInteger == 6) && filter.equals("tcp")){
            System.out.println("filter TCP");
            tcpFilter = true;
        }
        if ((protocolNumberInteger == 1) && filter.equals("icmp")){
            System.out.println("filter ICMP");
            icmpFilter = true;
        }*/
        // On saute le Header Checksum (2 octets)
        packetByteCount += (int)dataInputStream.skip(2);


        int compteurIp = 0;
        // IP Source
        byte [] ipSrcBuffer = new byte[4];
        packetByteCount += (int)dataInputStream.read(ipSrcBuffer);
        StringBuilder ipSrcSB = new StringBuilder();
        for (byte b : ipSrcBuffer){
            ipSrcSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp += 1;
            if (compteurIp != ipSrcBuffer.length) ipSrcSB.append(".");
        }
        compteurIp = 0;
        this.ipSrc = ipSrcSB.toString();
        System.out.println("Source IP: " + this.ipSrc);

        // IP Destination
        byte [] ipDestBuffer = new byte[4]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(ipDestBuffer);
        StringBuilder ipDestSB = new StringBuilder();
        for (byte b : ipDestBuffer){
            ipDestSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp += 1;
            if (compteurIp != ipDestBuffer.length) ipDestSB.append(".");
        }
            
        compteurIp = 0;
        this.ipDest = ipDestSB.toString();
        System.out.println("Destination IP: " + this.ipDest);
        
        if (protocolNumberInteger == IPv4Types.UDP){
            if ((filter.equals("tcp")) || (filter.equals("icmp"))){
                System.out.println("Le filtre " + filter + " empêche la lecture du reste de ce paquet.");
                packetByteCount += dataInputStream.skip(packetLength-packetByteCount); // On skip tout le reste si le filtre ne le permet pas 
            }
            else{
                Udp updSegment = new Udp();
                System.out.println("Bytes read so far: " + packetByteCount);
                updSegment.readUdp(dataInputStream, packetLength, packetByteCount);
            }
            
        }
        else if(protocolNumberInteger == IPv4Types.TCP){
            //si y'a un filtre sur autre chose que le tcp, on skippe lereste du paquet
            if ((filter.equals("udp")) || (filter.equals("icmp"))){
                System.out.println("Le filtre " + filter + " empêche la lecture du reste de ce paquet.");
                packetByteCount += dataInputStream.skip(packetLength-packetByteCount); // On skip tout le reste si le filtre ne le permet pas 
            }
            else{
                Tcp tcpSegment = new Tcp();
                System.out.println("Bytes read so far (before TCP): " + packetByteCount);
                // On lit tous les headers et on calcule le payload à lire
                // += car on renvoit la valeur du nb de bytes qu'on a lu depuis qu'on a commencé à parser les headers tcp
                int tcpBytesRead = tcpSegment.readTcp(dataInputStream, packetLength, packetByteCount);
                packetByteCount += tcpBytesRead; // On ajoute au compteur de bytes les bytes lues dans tcp
                this.payloadLength = tcpSegment.calculatePayloadLength(Integer.valueOf(this.totalLength), tcpSegment.getDataOffset());
                /*System.out.println("payload length: " + this.payloadLength);
                System.out.println("data offset: " + tcpSegment.getDataOffset());
                System.out.println("Bytes read so far: " + packetByteCount);
                System.out.println("Comment sauter direct au HTTP depuis où on en est dans le segment tcp?");*/
                packetByteCount += dataInputStream.skip(tcpSegment.getDataOffset()-tcpBytesRead); //On skip le reste de ce qui y'a à lire dans le segment tcp 
                /*System.out.println("packetByteCount: " + packetByteCount);
                System.out.println("totallength ethernet frame: " + this.totalLength);*/
            
                if (this.payloadLength != 0){
                    // HTTP
                    Http httpPayload = new Http();
                    packetByteCount = httpPayload.readHttp(dataInputStream, this.payloadLength, packetByteCount);//On met à jour packet bytes count qui vaut mnt la taille de la trame ethernet entière ==> totalLength
                }
                else {
                    System.out.println("No payload");
                    packetByteCount += (int)dataInputStream.skip(packetLength-packetByteCount);
                }
            }
        }
        // Bien que l'ICMP est dans la couche réseau comme l'IPv4
        // Si on rapproche du modèle OSI (ou TCP/IP jsais plus), l'ICMP est encapsulé dans l'IPv4
        else if(protocolNumberInteger == IPv4Types.ICMP){
            if ((filter.equals("tcp")) || (filter.equals("udp"))){
                System.out.println("Le filtre " + filter + " empêche la lecture du reste de ce paquet.");
                dataInputStream.skip(packetLength-packetByteCount); // On skip tout le reste si le filtre ne le permet pas 
            }
            else{
                Icmp icmp = new Icmp();
                packetByteCount = icmp.readIcmp(dataInputStream, packetLength, packetByteCount);
            }
            
        }
        // Pour lire la suite on part du principe que y'a pas d'options IPV4 (en réalité y'a que des options IPv4 si l'IHL est > 5)
        // En vrai go faire un if IHL < 5 faire ça et sinon mettre un msg pour dire que c'est pas supporté
        else{
            System.out.println("protocol n°" + protocolNumberInteger+ ": Non supporté");
            packetByteCount += (int)dataInputStream.skip(packetLength-packetByteCount);
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