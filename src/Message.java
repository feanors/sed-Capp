import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

class CustomPacketParser {
    
    private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    public static byte[] transformToServerFormat(long time, String msg) throws IOException {
        
        // Long to byte array
        dataOutputStream.writeLong(time);
        byte[] timeArr = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        
        // String to byte array
        byte[] msgArr = msg.getBytes();
        
        // Merge all into a single byte array, then return
        return mergeByteArrays(timeArr, msgArr);
        
    }

    public static long byteToLong(byte[] arr) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(arr, 0, arr.length);
        buffer.flip();
        return buffer.getLong();
    }
    
    private static byte[] mergeByteArrays(byte[] arr1, byte[] arr2) {
        byte[] merged = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, merged, 0, arr1.length);
        System.arraycopy(arr2, 0, merged, arr1.length, arr2.length);
        
        return merged;
    }
}

public class Message {
    // for pm and other special message type, todo if enough time
    public Integer messageType;
    
    public String message;
    public Long date;

    Message(String msg, Long date) {
        message = msg;
        this.date = date;
    }

    Message(DatagramPacket packet) {
        byte[] byteArr = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), byteArr, 0, packet.getLength());


        date = CustomPacketParser.byteToLong(Arrays.copyOfRange(byteArr, 0, 8));
        message = new String(Arrays.copyOfRange(byteArr, 8, byteArr.length));
    }

    public byte[] convertToByteArr() throws IOException {
        return CustomPacketParser.transformToServerFormat(date, message);
    }

    @Override
    public String toString() {
        return message.toString()+date.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if(!(o instanceof Message)) {
            return false;
        }

        Message m = (Message) o;
        return m.date.equals(date) && m.message.equals(message);
    }
}
