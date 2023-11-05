// import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import src.EtherTypes;
import src.Ethernet;

public class PcapReader{
    private int nbFrames = 0;
    public static void main(String[] args) {
        System.out.println("begin");
        if (args.length > 2) {
            System.err.println("Usage: java PcapReader <pcap_file> [FILTER]\n-\tex: filter=tcp. If there are no filter specified, the program will parse every Ethernet Frame");
            System.exit(1);
        }

        String pcapFile = args[0];
        String filter = "";
        if (args.length == 2)
            filter = args[1].split("=")[1].toLowerCase(); // ex: filter=tcp --> On prend tcp
        
        readPcapFile(pcapFile, filter);
    }

    public static void readPcapFile(String pcapFile, String filter) {
        int packetByteCount = 0; // Nombre d'octets lu depuis l'identification de la sous couche
        try {
            boolean isReading = true;
            Path filePath = Paths.get(pcapFile);
            FileInputStream fileInputStream = new FileInputStream(filePath.toString());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
            // Skip the pcap header (24 bytes)--> Pas besoin car c'est le header qui contient le magic number et les infos du fichier pcap
            dataInputStream.skip(24);

            int count = 1; // Pour afficher les n° de frames ethernet

            // Sortie du while si les 4 premiers octets (timestamp) renvoyés par le readEthernet est égale à false 
            while (isReading == true){
                Ethernet etherFrame = new Ethernet(filter); // COnstruction d'un Ethernet sans valeur, les valeurs seront remplies lors de la lecture
                isReading = etherFrame.readEthernet(dataInputStream, count);
                count++;
            }

            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}