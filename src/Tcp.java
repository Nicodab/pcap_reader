package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.IllegalArgumentException;

public class Tcp {
    private int portSrc; // header IPv4 de 2 octets,il donne la taille du paquets en octets
    private int portDest; // 2 octets, il donne l'id des paquets fragmenté ou jsp quoi
    private long nbSeq; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
    private long nbAck;
    private int[] dataOffset;
    private int dataOffsetDecimalValue;
    private int []flags; // [2]=URG [3]=ACK [4]=PSH [5]=RST [6]=SYN [7]=FIN
    private String []flagNames = {"CWR", "ECE", "URG", "ACK", "PSH", "RST", "SYN","FIN"}; // [2]=URG [3]=ACK [4]=PSH [5]=RST [6]=SYN [7]=FIN

    // Constructeur par défaut sans param sauf le nb d'octets lu jusqu'à maintenant
    public Tcp(){
        this.portSrc = -1;
        this.portDest = -1;
        this.nbSeq = -1;
        this.nbAck = -1;
        this.flags = new int[8]; //Tableau de 'bit' représentant un octet
        this.dataOffset = new int[4];
        this.dataOffsetDecimalValue = 0;
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public int readTcp(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        int bytesCountTcp = 0;
        System.out.println("\n----------- TCP segment -----------");
        // portSrc 
        byte [] portSrcBuffer = new byte[2]; // Protocol type: 2 octets
        int srcBytes = (int)dataInputStream.read(portSrcBuffer);
        bytesCountTcp += srcBytes;
        packetByteCount += srcBytes;
        this.portSrc = convertirEnDecimal(portSrcBuffer);
        System.out.println("> Source Port: " + this.portSrc);

        // portDest
        byte [] portDestBuffer = new byte[2]; // Protocol type: 2 octets
        int destBytes = (int)dataInputStream.read(portDestBuffer);
        bytesCountTcp += destBytes;
        packetByteCount += destBytes;
        this.portDest = convertirEnDecimal(portDestBuffer);
        System.out.println("> Destination Port: " + this.portDest);

        // nbSequence
        byte [] nbSequenceBuffer = new byte[4]; // Protocol type: 4 octets
        int seqBytes = (int)dataInputStream.read(nbSequenceBuffer);
        bytesCountTcp += seqBytes;
        packetByteCount += seqBytes;

        this.nbSeq = (long)convertirEnDecimal(nbSequenceBuffer);
        System.out.println("> Sequence Number: " + this.nbSeq);

        // nbAcknoledgement
        byte [] nbAckBuffer = new byte[4]; // Protocol type: 4 octets
        int ackBytes = (int)dataInputStream.read(nbAckBuffer);
        bytesCountTcp += ackBytes;
        packetByteCount += ackBytes;
        this.nbAck = (long)convertirEnDecimal(nbAckBuffer);
        System.out.println("> Acknoledgment Number: " + this.nbAck);

        //packetByteCount += dataInputStream.skip(1); // On saute data offset (4bits) et reserved (4bits)

        // dataOffset (4bits) et reserved (4bits) --> l'offset nous donne la taille du segment tcp
        // Si c'est 5 --> On a 5 * 32 bits à lire --> 5 * 4 octets -> Y'a du padding parfois si nécessaire
        byte [] dataOffsetBuffer = new byte[1];
        int offsetBytes = (int)dataInputStream.read(dataOffsetBuffer);
        bytesCountTcp += offsetBytes;
        packetByteCount += offsetBytes;
        this.dataOffset = separateBitPerBitForDataOffset(dataOffsetBuffer[0]);
        /*for (int i = 0; i < dataOffset.length; i++){
            System.out.print(this.dataOffset[i]);
        }*/

        // *4 car ça nous donne le nb de word à 32 bit dans le segment
        // --> 32 bits = 4 octets, donc dataOffsetDecimalValue est le nb d'octets contenue dans les data du segments tcp
        this.dataOffsetDecimalValue = fourBitsToDecimal(this.dataOffset) * 4;
        System.out.println("> Data Offset: " + this.dataOffsetDecimalValue + " octets");
        
        // FLAGS
        // Ce qui nous intéresse ça va être juste flag[]
        // flag[2]=URG, flag[3]=ACK flag=[4]=PSH, flag[5]=RST; flag[6]=SYN, flag[7]=FIN
        System.out.print("> The flags set are: ");
        byte [] flagsBuffer = new byte[1]; // flags: 1 octet
        int flagBytes = (int)dataInputStream.read(flagsBuffer);
        bytesCountTcp += flagBytes;
        packetByteCount += flagBytes;
        this.flags = separateBitPerBit(flagsBuffer[0]);
        for (int i = 0; i < flags.length; i++){
            if (flags[i] == 1){
                System.out.print(this.flagNames[i] + " ");
            }
        }
        System.out.println();

        int skipTheRest = (int)dataInputStream.skip(6);
        bytesCountTcp += skipTheRest;
        packetByteCount += skipTheRest; // skip window size, checksum urgent pointer
        

        //System.out.println("on skip le reste pour l'instant");
        //dataInputStream.skip(packetLength-packetByteCount); A//A REMETTRE SUREMENT OU REVOIR Comment sauter el reste du segment tcp (skip(dataoffset-bytes comptés depuis le début de tcp))

        return bytesCountTcp;
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

    public static int[] separateBitPerBit(byte octet) {
        int []bits = new int[8];
        for (int i = 7; i >= 0; i--) {
            bits[bits.length-1-i] = (octet >> i) & 1;// on le remplit de gauche à droite du tableau pour le lire normal
        }
        System.out.println();
        return bits;
    }

    public static int[] separateBitPerBitForDataOffset(byte octet) {
        int []bits = new int[8];
        int []offset = new int [4]; // Le offset qu'on va extraire du byte (4 premiers bits)
        // Séparation en tableau de 8 bits
        for (int i = 7; i >= 0; i--) {
            bits[bits.length-1-i] = (octet >> i) & 1;// on le remplit de gauche à droite du tableau pour le lire normal
        }
        // Extirper les 4 premiers bits pour découvrir la taille du segment TCP
        for (int i = 0; i < offset.length; i++)
            offset[i] = bits[i];

        return offset;
    }

    public static int fourBitsToDecimal(int[] bits) throws IllegalArgumentException{
        if (bits.length != 4)
            throw new IllegalArgumentException("Le tableau doit avoir exactement 4 éléments.");

        int decimalValue = 0;    
        for (int i = 0; i < 4; i++) {
            if (bits[i] != 0 && bits[i] != 1) 
                throw new IllegalArgumentException("Chaque élément du tableau doit être 0 ou 1.");
            decimalValue = (decimalValue << 1) | bits[i];
        }
    
        return decimalValue;
    }

    // Get la valeur de l'offset
    public int getDataOffset()  {return this.dataOffsetDecimalValue;}

    // Payload length = totalLength - 20 (=> taille d'un header IPv4) - data offset (taille des headers tcp)  
    public int calculatePayloadLength(int totalLength, int dataOffset){
        return (totalLength - 20 - dataOffset);
    }
}