package src;

import java.io.DataInputStream;
import java.io.IOException;
import src.IPv4Types;
import src.DnsQuestion;
import src.DnsAdditionalRR;
import src.DnsAnswer;

public class Dns {
    private int flags;
    private int question;
    private int nbAnswerRR;
    private int nbAuthorityRR;
    private int nbAdditionalRR;
    private int name; // header IPv4 de 2 octets,il donne la taille du paquets en octets
    private int rrType; // 2 octets, il donne l'id des paquets fragmenté ou jsp quoi
    private int rrClass; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
    private int ttl;
    private int rdLength;
    private int QR; // 0 for Dns query, 1 for Dns Response 


    // Constructeur par défaut sans param sauf le nb d'octets lu jusqu'à maintenant
    public Dns(){
        question = -1;
        nbAnswerRR = -1;
        nbAuthorityRR = -1;
        nbAdditionalRR = -1;
        name = -1; // header IPv4 de 2 octets,il donne la taille du paquets en octets
        rrType = -1; // 2 octets, il donne l'id des paquets fragmenté ou jsp quoi
        rrClass = -1; // Protocol number: 1 octet. Il donne par ex 17 pour une encapsulat° UDP, 6 pour du TCP 
        ttl = -1;
        rdLength = -1;
    }

    // Lire le Record DNS et son contenu
    public void readDns(DataInputStream dataInputStream, int packetLength, int packetByteCount) throws IOException {
        
        System.out.println("----------- DNS Records -----------");
        //packetByteCount += dataInputStream.skip(2); // 2 octets pour le Transact° ID


        byte [] transacBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(transacBuffer);
        StringBuilder transacSB = new StringBuilder();
        for (byte b : transacBuffer){
            transacSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        }
        System.out.println("Transac: " + transacSB.toString());

        // Flags --> On lit les 2 octets d'après mais en ralité, y'a que le 1er bit qui nous intéresse qui est le QR
        // Query quand QR = 0, response quand QR = 1
        byte [] flagBuffer = new byte[1]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(flagBuffer);
        StringBuilder flagSB = new StringBuilder();
        for (byte b : flagBuffer){
            flagSB.append(String.format("%02X", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
            QR = getFirstBitFromByte(b);
        }
        this.flags = Integer.valueOf(flagSB.toString());
        System.out.println("Flags: " + this.flags);
        if (QR == 0)
            System.out.println("Dns message: Query");
        else if (QR == 1)
            System.out.println("Dns message: Response");

        packetByteCount += dataInputStream.skip(1); // 1 octet skippé car on prend que le 1er octet (sur 2) de flag

        // question 
        byte [] questionBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(questionBuffer);
        StringBuilder questionSB = new StringBuilder();
        for (byte b : questionBuffer)
            questionSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.question = Integer.valueOf(questionSB.toString());
        System.out.println("Question: " + this.question);
        
        // nbAnswerRR 
        byte [] nbAnswerRRBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(nbAnswerRRBuffer);
        StringBuilder nbAnswerRRSB = new StringBuilder();
        for (byte b : nbAnswerRRBuffer)
            nbAnswerRRSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.nbAnswerRR = Integer.valueOf(nbAnswerRRSB.toString());
        System.out.println("Answer RRs: " + this.nbAnswerRR);

        // nbAuthorityRR 
        byte [] nbAuthorityRRBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(nbAuthorityRRBuffer);
        StringBuilder nbAuthorityRRSB = new StringBuilder();
        for (byte b : nbAuthorityRRBuffer)
            nbAuthorityRRSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.nbAuthorityRR = Integer.valueOf(nbAuthorityRRSB.toString());
        System.out.println("Authority RRs: " + this.nbAuthorityRR);

        // nbAdditionalRR 
        byte [] nbAdditionalRRBuffer = new byte[2]; // Protocol type: 2 octets
        packetByteCount += (int)dataInputStream.read(nbAdditionalRRBuffer);
        StringBuilder nbAdditionalRRSB = new StringBuilder();
        for (byte b : nbAdditionalRRBuffer)
            nbAdditionalRRSB.append(String.format("%d", b & 0xFF)); // Masquage avec 0xFF pour afficher en décimal
        
        this.nbAdditionalRR = Integer.valueOf(nbAdditionalRRSB.toString());
        System.out.println("Additional RRs: " + this.nbAdditionalRR);


        // Questions à lire 
        if (this.question > 0){
            // On lit Les questions
            for (int i = 0; i < this.question; i++){
                DnsQuestion question = new DnsQuestion();
                // Mise à jour du packetByteCount qui a été iuncrémenté dans une méthode de la classe DnsQuestion
                packetByteCount = question.readQuestionSection(dataInputStream, packetLength, packetByteCount);
            }
        }

        // Questions à lire 
        if (this.nbAnswerRR > 0){
            // On lit Les questions
            for (int i = 0; i < this.nbAnswerRR; i++){
                DnsAnswer answer = new DnsAnswer();
                // Mise à jour du packetByteCount qui a été iuncrémenté dans une méthode de la classe DnsQuestion
                packetByteCount = answer.readAnswer(dataInputStream, packetLength, packetByteCount);
            }
        }

        // Additionalrecords à lire 
        if (this.nbAdditionalRR > 0){
            // On lit Les questions
            for (int i = 0; i < this.nbAdditionalRR; i++){
                DnsAdditionalRR additionalRR = new DnsAdditionalRR();
                // Mise à jour du packetByteCount qui a été iuncrémenté dans une méthode de la classe DnsQuestion
                packetByteCount = additionalRR.readAdditionalRecordSection(dataInputStream, packetLength, packetByteCount);
            }
        }

        //Le rest c'est le msg du DNS
        // pour l'instant on skip
        System.out.println("On saute la lecture du msg DNS car on ne sait pas lire pour le moment");
        dataInputStream.skip(packetLength-packetByteCount);
    }

    public static void readBitperBit(byte octet) {
        int []bits = new int[8];
        for (int i = 0; i < 8; i++) {
            int bit = (octet >> i) & 1;
            System.out.print(bit);
        }
        System.out.println();
    }

    public static int getFirstBitFromByte(byte octet){
        return (octet >> 7) & 1;
    }

    public static void readQName(){

    }
    
}