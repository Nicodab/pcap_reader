import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class inversion {
    public static void main(String[] args) {
        byte[] hex = new byte[4];
        hex[0] = (byte) 0xFD;
        hex[1] = (byte) 0x8A;
        hex[2] = (byte) 0x31;
        hex[3] = (byte) 0x65;

        // Créez un ByteBuffer avec l'ordre Little-Endian
        ByteBuffer buffer = ByteBuffer.wrap(hex);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Ajout de l'ordre Little-endian.

        // Lisez le ByteBuffer en tant qu'entier
        int decimalValue = buffer.getInt();

        // Affichez la valeur décimale
        System.out.println("Valeur décimale : " + decimalValue);
    }


    
}
