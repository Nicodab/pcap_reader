package src;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import src.Arp;
import src.IPv4;

public class Ethernet {
    private String timestamp;
    private int packetLength; //Nb d'octets à lire dans les données encaspulées de la trame Ethernet.
    private String macDest; // Mac Dest
    private String macSrc; // Mac Source 
    private String etherType; // Type de paquets de la sous-couche (ARP, UDP, TCP, ...)
    private int packetByteCount; //Nb d'octets lus dans les données encaspulées de la trame Ethernet.
    private String filter;

    // Constructor sans param par défaut
    public Ethernet(String filter){
        timestamp = "";
        packetLength = -1;
        macDest = "";
        macSrc = "";
        etherType = "";
        packetByteCount = 0;
        this.filter = filter;
    }

    // Constructor avec param
    public Ethernet(String _timestamp, int _packetLength, String _macDest, String _macSrc, String _etherType, int _packetByteCount){
        timestamp = _timestamp;
        packetLength = _packetLength;
        macDest = _macDest;
        macSrc = _macSrc;
        etherType = _etherType;
        packetByteCount = _packetByteCount;
    }

    // Lire que lespremiers info jusqu'à l'etherType
    // dataInpustStream pour faire avancer le buffer selon les infos à lire dans une trame Ethernet
    public boolean readEthernet(DataInputStream dataInputStream, int nbFrame) throws IOException{
        //Read time stamp
        byte [] timestamp = new byte[4]; // Le BUFFER "timestamp" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        int timestampRead = dataInputStream.read(timestamp);
        if (timestampRead == -1 ) return false; // Plus rien à lire
        System.out.println("\n*****************************************");
        System.out.println("*\t\tFRAME n°" + nbFrame + "\t\t*");
        System.out.println("*****************************************");
        System.out.println("Timestamp: " + String.valueOf(inverserEtConvertir(timestamp)) + "s");
        this.timestamp = String.valueOf(inverserEtConvertir(timestamp)); // Pour l'envoyer dans la classe
        dataInputStream.skip(4); // micro ou nano secondes sautées (4 octets)
        
        //Read capturePacketLength
        byte [] capturePacketLength = new byte[4]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        int capturePacketLengthRead = dataInputStream.read(capturePacketLength);
        // On initialise ici le packetLength de la classe Ethernet
        this.packetLength = inverserEtConvertir(capturePacketLength); // --> A réutiliser pour compter jusqu'à ce qu'on arrive bien à la fin du paquets pour passer à la suivante
        System.out.println("> Capture packet length: " + String.valueOf(inverserEtConvertir(capturePacketLength)) + " octets");
        dataInputStream.skip(4); // skip 4 car les 4 octets suivants ne sont pas intéressant 
        
        int compteurMac = 0;
        //Read capturePacketLength
        byte [] macAdressDest = new byte[6]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        // C'est à partir de ici qu'on compte les données de la trame ethernet
        // Car après le packet length on sait cmb d'octets on va lire
        packetByteCount += dataInputStream.read(macAdressDest);
        System.out.print("> Destination Mac: ");
        StringBuilder macDestSB = new StringBuilder();
        // Affichez les octets inversés en décimal
        for (byte b : macAdressDest) {
            System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            macDestSB.append(String.format("%02X", b & 0xFF));
            compteurMac +=  1;
            if (compteurMac != macAdressDest.length) macDestSB.append(":"); // On ajoute 2 point tous les octets (les 2 carac hexa)
        }
        this.macDest = macDestSB.toString(); // affectation à l'attribut de la classe 

        compteurMac = 0;
        System.out.println();

        //Read capturePacketLength
        byte [] macAdressSrc = new byte[6]; // Le BUFFER "capturePacketLength" fait 4 octets quoi qu'il arrive suite au 24 octets skippés
        packetByteCount += dataInputStream.read(macAdressSrc);
        StringBuilder macSrcSB = new StringBuilder();
        // Affichez les octets inversés en décimal
        System.out.print("> Source Mac: ");
        for (byte b : macAdressSrc) {
            System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            macSrcSB.append(String.format("%02X", b & 0xFF));
            compteurMac +=  1;
            if (compteurMac != macAdressDest.length) macDestSB.append(":");
        }
        compteurMac = 0;
        System.out.println();

        //EtherType
        byte [] etherTypeBuffer = new byte[2]; // Le BUFFER "etherType" fait 2 octets
        packetByteCount += dataInputStream.read(etherTypeBuffer);
        StringBuilder etherTypeSb = new StringBuilder();
        System.out.print("> Ether Type: ");
        for (byte b : etherTypeBuffer) {
            System.out.printf("%02X", b & 0xFF); // Masquage avec 0xFF pour afficher en décimal
            etherTypeSb.append(String.format("%02X", b & 0xFF));
        }
        this.etherType = etherTypeSb.toString();
        System.out.println();

        // ARP
        if (this.etherType.equals(EtherTypes.ARP)){
            Arp arpPacket = new Arp();
            arpPacket.readARP(dataInputStream, packetLength, packetByteCount);
        }
        // IPv4
        else if (this.etherType.equals(EtherTypes.IPv4)){
            IPv4 ipPacket = new IPv4();
            ipPacket.readIPv4(dataInputStream, packetLength, packetByteCount,filter);
        }
        //Quand l'etherType n'est d'aucun type reconnu par mon code, on skip le nb de bytes restants à lire, soit (packetLength-packetByteCount)
        else {
            System.out.println("Packet not supported for the moment");
            this.packetByteCount = (int)dataInputStream.skip(packetLength-packetByteCount); // pour le moment on skippe la diff de quantité de données qu'on ne sait pas encore lire 
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // Inverser et convertir tout un ensmeble d'octets pour le little/big endian
    public static int inverserEtConvertir(byte [] byteArray){
        // Créez un ByteBuffer avec l'ordre Little-Endian
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Lisez le ByteBuffer en tant qu'entier
        int decimalValue = buffer.getInt();
        return decimalValue;
    }

    public static byte[] inverserTableauBytes(byte[] octets) {
        int longueur = octets.length;
        byte[] tableauInverse = new byte[longueur];
    
        for (int i = 0; i < longueur; i++) {
            tableauInverse[i] = octets[longueur - 1 - i];
        }
    
        return tableauInverse;
    }


    // Inverser et convertir octets par octets
    public static byte[] inverserOctetParOctet(byte [] byteArray){
        // Inversez les octets
        byte[] reversedBytes = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            reversedBytes[i] = byteArray[byteArray.length - 1 - i];
        }
        return reversedBytes;
    }
}
