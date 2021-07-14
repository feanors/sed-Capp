import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class UDPHandler {
    private MulticastSocket localSocket;
    private MulticastSocket relaySocket;
    
    private InetAddress address;
    private boolean relayRole;

    JTextArea textArea;
    JTextArea usersArea;

    ArrayList<Message> localHandledMessages = new ArrayList<>();
    ArrayList<Message> relayHandledMessages = new ArrayList<>();

    ArrayList<String> userNameList = new ArrayList<>();

    UDPHandler(String zerotierInterfaceName, boolean relayRole, JTextArea textArea, JTextArea usersArea) throws IOException {
        

        this.usersArea = usersArea;
        this.textArea = textArea;

        this.relayRole = relayRole;
        
        localSocket = new MulticastSocket(6788);
        localSocket.setNetworkInterface(NetworkInterface.getByName(zerotierInterfaceName));
        localSocket.joinGroup(InetAddress.getByName("ff02::1"));

        if(relayRole) {
            relaySocket = new MulticastSocket(6789);
            // interface name of master subnet, currently hard coded, can be an input from the user
            relaySocket.setNetworkInterface(NetworkInterface.getByName("ztrf2q4tkf"));
            relaySocket.joinGroup(InetAddress.getByName("ff02::1"));
        }

        address = InetAddress.getByName("ff02::1");
    }

    public void sendMsg(Message m) throws IOException {
        synchronized(localHandledMessages) {
            byte[] buffer = new byte[2048];

            if(!localHandledMessages.contains(m)) {
                localHandledMessages.add(m);

                buffer = m.convertToByteArr();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 6788);
                localSocket.send(packet);
                
            }
        
        }
    }

    public void sendRelayMsg(Message m) throws IOException {
        synchronized(relayHandledMessages) {
            byte[] buffer = new byte[2048];

            if(!relayHandledMessages.contains(m)) {
                relayHandledMessages.add(m);
            
                buffer = m.convertToByteArr();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 6789);
                relaySocket.send(packet);
            }
        }
    }

    public void listenOnLocal() throws IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {

                        byte[] buffer = new byte[2048];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        localSocket.receive(packet);
                        
                        Message m = new Message(packet);

                        // dont print the self message
                        synchronized(localHandledMessages) {
                            if(!localHandledMessages.contains(m)) {
                                textArea.setText(textArea.getText() + m.message + "\n");
                                System.out.println(m.message);
                                localHandledMessages.add(m);
                            }
                        }

                        synchronized(userNameList) {
                            String userName = m.message.substring(0, m.message.indexOf(" :"));
                            if (!userNameList.contains(userName)) {
                                userNameList.add(userName);
                                usersArea.setText(usersArea.getText() + userName + "\n");
                            }

                        }

                        if(relayRole) {
                            synchronized(relayHandledMessages) {
                                // new message from local, send as relay
                                if(!relayHandledMessages.contains(m)) {
                                    sendRelayMsg(m);
                                }
                            }
                        }
                    }
                } catch(IOException ioException) {
                    System.out.println(ioException);
                }
            }
        }).start();
    }

    public void listenOnRelay() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        byte[] buffer = new byte[2048];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        relaySocket.receive(packet);
                        
                        Message m = new Message(packet);
                        

                        // don't relay messages coming from master subnet
                        synchronized(relayHandledMessages) {
                            if(!relayHandledMessages.contains(m)) {
                                relayHandledMessages.add(m);
                            }
                        }

                        synchronized(localHandledMessages) {
                            if(!localHandledMessages.contains(m)) {
                                textArea.setText(textArea.getText() + m.message + "\n");
                                System.out.println(m.message);
                                if(relayRole) {
                                    sendMsg(m);
                                }
                            }
                        }
                        
                    }
                } catch(IOException ioException) {
                    System.out.println(ioException);
                }
            }
        }).start();
    }
}
