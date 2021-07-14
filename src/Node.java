import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.Scanner;

import java.awt.event.*;  
import javax.swing.*; 

public class Node {
    private boolean relayRole;

    private UDPHandler udpHandler;

    private Scanner scanner;

    JTextArea textArea;
    JTextArea users;

    private String userName;

    public Node(JTextArea textArea, JTextArea users) throws IOException {

        this.textArea = textArea;
        this.users = users;
        

        scanner = new Scanner(System.in);

        String interfaceName = getInterfaceName();
        relayRole = getRelayNodeStatus();
        userName = getUserName();

        udpHandler = new UDPHandler(interfaceName, relayRole, textArea, users);

        listenLocal();
        if(relayRole) {
            listenOnRelay();
        } 

        System.out.println("You can start messaging now (: ");
    }

    String getInterfaceName() {
        System.out.println("Enter the zerotier interface name");
        return scanner.nextLine();
    }

    boolean getRelayNodeStatus() {
        System.out.println("Would you like to be a relay node (y/n)");
        return scanner.nextLine().equalsIgnoreCase("y") == true;
    }

    String getUserName() {
        System.out.println("Enter your user name");
        return scanner.nextLine();
    }

    void sendMessage(String msg) throws IOException {
        Message m = new Message(userName + " : " + msg, new Date().getTime());
        textArea.setText(textArea.getText() + m.message + "\n");
        udpHandler.sendMsg(m);
    }

    void listenLocal() throws IOException {
        udpHandler.listenOnLocal();
    }

    void listenOnRelay() {
        if(relayRole) {
            udpHandler.listenOnRelay();
        }
    }

    public String getMessage() {
        return scanner.nextLine();
    }

    public static void main(String[] args) throws IOException {
        
        

        JFrame f = new JFrame("Semi-Dec Chat App");  

        JLabel description = new JLabel("Non-Encrypted Chat Application");
        description.setBounds(140, 20, 200, 40);

        JLabel label = new JLabel("Enter a message:");
        label.setBounds(20,70,120,40);

        JTextField tf = new JTextField();
        tf.setBounds(130,70, 200, 40);  


        JTextArea textArea = new JTextArea();
        textArea.setBounds(10, 150, 240, 400);

        JLabel onlineUsersLabel = new JLabel("Online Users");
        onlineUsersLabel.setBounds(340, 160, 80, 40);

        JTextArea onlineUsersArea = new JTextArea();
        onlineUsersArea.setBounds(270, 230, 200, 320);

        
        JButton b = new JButton("Send");  
        b.setBounds(330,70,100,40); 

        Node n = new Node(textArea, onlineUsersArea);

        b.addActionListener(new ActionListener(){  
            public void actionPerformed(ActionEvent e) {
                    try {
                        n.sendMessage(tf.getText()); 
                        tf.setText("");
                    } catch(IOException e2) {
                        System.out.println(e2);
                    }
                }  
            });  
        

        f.add(b);
        f.add(description);
        f.add(onlineUsersLabel);
        f.add(tf);
        f.add(onlineUsersArea);
        f.add(textArea); 
        f.add(label); 
        f.setSize(500,600);  
        f.setLayout(null);  
        f.setVisible(true);   
    }
}
