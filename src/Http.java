package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Http {
    private String headers;
    private String content;

    public Http(){
        this.headers = "";
        this.content = "";
    }

    // Faire une classe ARP dans laquelle il y aurait cette fonction ainsi que ses propres attributs
    public int readHttp(DataInputStream dataInputStream, int payloadLength, int packetByteCount) throws IOException {
        int bytesCountTcp = 0;
        System.out.println("----------- HTTP PAYLOAD -----------");
        
        // Payload HTTP
        byte [] payloadBuffer = new byte[payloadLength]; // On lit tout le reste de la trame qui est le payload
        packetByteCount += (int)dataInputStream.read(payloadBuffer);

        // Convertir les données brutes en un string
        String httpData = new String(payloadBuffer, StandardCharsets.UTF_8);
        
        String[] httpParts = httpData.split("\r\n");

        if (httpParts.length >= 1) {
            String headers = httpParts[0];  // En-têtes HTTP

            // Vous pouvez analyser les en-têtes ici
            System.out.println("En-têtes HTTP : " + headers);

            if (httpParts.length >= 2) {
                String content = String.join("\r\n", Arrays.copyOfRange(httpParts, 1, httpParts.length));

                // Vous pouvez analyser le contenu ici
                System.out.println("Contenu : " + content);
            }
        }

        return packetByteCount;
    }
}
