package src;

import java.io.DataInputStream;
import java.io.IOException;

public class Arp {
    private String protocolType;
    private String operation;
    private String macSrc;
    private String ipSrc;
    private String macDest;
    private String ipDest;

    // Constructeur par défaut sans param sauf le nb d'octets lu jusqu'à maintenant
    public Arp(){
        this.protocolType = "";
        this.operation = "";
        this.macSrc = "";
        this.ipSrc = "";
        this.macDest = "";
        this.ipDest = "";
    }

    public Arp(String protocolType, String operation, String macSrc, String ipSrc, String macDest, String ipDest){
        this.protocolType = protocolType;
        this.operation = operation;
        this.macSrc = macSrc;
        this.ipSrc = ipSrc;
        this.macDest = macDest;
        this.ipDest = ipDest;
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public void readARP(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        // byte [] macAdressDest = new byte[6]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        // int macAdressDestRead = dataInputStream.read(macAdressDest);
        System.out.println("\n----------- ARP Packet -----------");
        // On saute le harware type
        packetByteCount += (int)dataInputStream.skip(2); // Hardwaretype non intéressant pour le projet | cast en int car ça renvoie unlong
        byte [] protocolType = new byte[2]; // Protocol type: 2 octets
        packetByteCount += dataInputStream.read(protocolType);

        StringBuilder hexProtocolTypeString = new StringBuilder();
        for (byte b : protocolType)
            hexProtocolTypeString.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.protocolType = hexProtocolTypeString.toString(); // init du protocltype en str 
        // Si c'est de l'IPv4
        if (this.protocolType.equals("0800")) System.out.println("Protocol Type: IPv4");
        //Si c'est pas del'IP --> On l'affiche en brute
        else {
            System.out.print("> Protocol Type: ");
            for (byte b : protocolType)
                System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
        }

        // On saute 1 octet pour le Hardware Length & 1 pour le Protocol Length
        packetByteCount += (int)dataInputStream.skip(2); // cast en int car ça renvoit un long
        
        byte [] operation = new byte[2]; // Protocl type: 2 octets
        packetByteCount += dataInputStream.read(operation);

        StringBuilder hexOperationString = new StringBuilder();
        for (byte b : operation)
            hexOperationString.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        this.operation = hexOperationString.toString();
        if (this.operation.equals("0001")) System.out.println("Operation: Request");
        else System.out.println("> Operation: Reply");

        int compteurMac = 0;
        //Read capturePacketLength
        byte [] macAdressSrc = new byte[6]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        packetByteCount += dataInputStream.read(macAdressSrc);
        StringBuilder macSrcSB = new StringBuilder();
        System.out.print("> Sender Mac: ");
        // Affichez les octets inversés en décimal
        for (byte b : macAdressSrc) {
            //System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            macSrcSB.append(String.format("%02X", b & 0xFF));
            compteurMac +=  1;
            if (compteurMac != macAdressSrc.length) macSrcSB.append(":");
        }
        this.macSrc = macSrcSB.toString();
        System.out.println(this.macSrc);
        compteurMac = 0;
        //System.out.println("Dest Mac Adress: " + String.valueOf(inverserEtConvertir(macAdressDest)) + "\n");
        //dataInputStream.skip(6); // skip 6 car on en a lu 6
        int compteurIp = 0;
        //Read capturePacketLength
        byte [] ipSender = new byte[4]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        packetByteCount += dataInputStream.read(ipSender);
        StringBuilder ipSrcSB = new StringBuilder();
        // Affichez les octets inversés en décimal
        System.out.print("> Sender IP: ");
        for (byte b : ipSender) {
            //System.out.printf("%d", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            ipSrcSB.append(String.format("%d", b & 0xFF));
            compteurIp +=  1;
            if (compteurIp != ipSender.length) ipSrcSB.append(".");
        }
        compteurIp = 0;
        this.ipSrc = ipSrcSB.toString();
        System.out.println(this.ipSrc);
        // dataInputStream.skip(6); // skip 6 car on en a lu 6

        byte [] macAdressDest = new byte[6]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        packetByteCount += dataInputStream.read(macAdressDest);
        StringBuilder macDestSB = new StringBuilder();
        System.out.print("> Destination Mac: ");
        // Affichez les octets inversés en décimal
        for (byte b : macAdressDest) {
            //System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            macDestSB.append(String.format("%02X", b & 0xFF));
            compteurMac +=  1;
            if (compteurMac != macAdressDest.length) macDestSB.append(":");
        }
        compteurMac = 0;
        this.macDest = macDestSB.toString();
        System.out.println(this.macDest);
        //System.out.println("Dest Mac Adress: " + String.valueOf(inverserEtConvertir(macAdressDest)) + "\n");
        //dataInputStream.skip(6); // skip 6 car on en a lu 6
        //Read capturePacketLength
        byte [] ipDest = new byte[4]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        packetByteCount += dataInputStream.read(ipDest);
        StringBuilder ipDestSB = new StringBuilder();
        // Affichez les octets inversés en décimal
        System.out.print("> Destination IP: ");
        for (byte b : ipDest) {
            ipDestSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            compteurIp +=  1;
            if (compteurIp != ipDest.length) ipDestSB.append(".");
        }
        compteurIp = 0;
        this.ipDest = ipDestSB.toString();
        // dataInputStream.skip(6); // skip 6 car on en a lu 6
        System.out.println(this.ipDest);
        dataInputStream.skip(packetLength-packetByteCount); // on lit la diff (si y'a du padding)
        packetByteCount += packetLength-packetByteCount;
        // C'est bon on arrive à gérer le padding proprement pour terminer le paquet donc go schématiser tout ça et faire des classes propres avec de l'héritage
    }
}
