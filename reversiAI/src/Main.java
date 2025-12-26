
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Java class managing the communictaion to the server
 */
public class Main {
    public static Socket socket;
    public static DataOutputStream dataOut;
    public static DataInputStream dataIn;

    public static ReversiGame RG;

    public static boolean gameEnd;
    public static boolean compare;

    /**
     * Methode called when executing the swp2025_group6.jar file.
     * Expects the servers port and ip as arguments, an additional -c flag compares
     * MiniMax and alpha-beta pruning.
     * Reads out the map and player number given from the server.
     * Reads out further server messages and forwards them to the ReversiGame
     * Object.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String serverID = "";
        gameEnd = false;
        socket = new Socket();
        boolean connected = true;
        String port = "";
        int playerNumber = 0;

        // Check the Parameters

        if (args == null || args.length == 0) {
            System.out.println("Bitte geben sie eine korrekte Portnummer an!");
        } else if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
            System.out.println("Das ist die Reversi Ai der Gruppe 6!");
            System.out.println("Nutzen sie die Parameter -p und -s um eine Portnummer und serveraddresse anzugeben,");
            System.out.println("zum vergleichen das -c flag zusaetzlich angeben");
        } else if (!(args.length == 4 || args.length == 5)) {
            System.out.println("Die übergebenen Parameter stimmen nicht mit den erwarteten überein");
        } else {
            if ((args[0].equals("-s") || args[0].equals("--server"))
                    && (args[2].equals("-p") || args[2].equals("--port")) && args.length == 4) {
                serverID = args[1];
                port = args[3];
            }

            else if ((args[2].equals("-s") || args[2].equals("--server"))
                    && (args[0].equals("-p") || args[0].equals("--port")) && args.length == 4) {
                port = args[1];
                serverID = args[3];
            } else if ((args[2].equals("-s") || args[2].equals("--server"))
                    && (args[0].equals("-p") || args[0].equals("--port")) &&
                    (args[4].equals("-c") || args[0].equals("--compare"))) {
                port = args[1];
                serverID = args[3];
                compare = true;
            } else if ((args[0].equals("-s") || args[0].equals("--server"))
                    && (args[2].equals("-p") || args[2].equals("--port")) &&
                    (args[4].equals("-c") || args[0].equals("--compare"))) {
                port = args[3];
                serverID = args[1];
                compare = true;
            } else {
                System.out.println("Die übergebenen Parameter stimmen nicht mit den erwarteten überein");
            }

            // Paramter korrekt,
            // versuche eine Verbindung aufzubauen

            try {
                socket.connect(new InetSocketAddress(serverID, Integer.parseInt(port)), 1000);
            } catch (ConnectException e) {
                System.out.println("Connection Failed!");
                connected = false;
            }

            // Datenübertragung mit dem Server

            if (connected) {
                // initialisierung der Data-Streams
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());
                // übermitteln der eigenen Gruppennummer
                dataOut.write(1);
                dataOut.writeInt(1);
                dataOut.write(6);

                byte b = dataIn.readByte();
                int length = 0;
                String Message = "";
                String map = "";

                // Read out map
                if (b == 2) {
                    length = dataIn.readInt();
                    byte[] MapBytes = new byte[length];
                    dataIn.read(MapBytes, 0, length);
                    for (int i = 0; i < length; i++) {
                        map += (char) MapBytes[i];
                    }
                } else {
                    System.out.println("Falsche Nachricht Erwartet");
                }
                // Read out player number
                b = dataIn.readByte();
                if (b == 3) {
                    length = dataIn.readInt();
                    playerNumber = (int) dataIn.readByte();
                } else {
                    System.out.println("Falsche Nachricht Erwartet");
                }

                // Create new ReversiGame object with the received map and player number
                RG = new ReversiGame(map, playerNumber);
                // Receive all server messages, call corresponding ReversiGame functions
                while (!gameEnd) {
                    // no busy waiting
                    b = dataIn.readByte();
                    length = dataIn.readInt();
                    byte[] MessageBytes = new byte[length];
                    dataIn.read(MessageBytes, 0, length);

                    switch (b) {
                        case 4 -> sendMove(RG.makeMove(MessageBytes, compare));
                        case 6 -> RG.moveMade(MessageBytes);
                        case 7 -> {
                        }
                        case 8 -> RG.setGamePhase(2);
                        case 9 -> gameEnd = true;
                        default -> System.out.println("Anderen Nachrichtentypen erwartet");
                    }

                    Message = "";
                }

                //DataManager.generateCSV(); //(only use when measuring localy)
                // Game ended, clean up
                RG.gameEnd();
                dataOut.close();
                dataIn.close();
                socket.close();
            }
        }
    }

    /**
     * Writes a given move into the dataOut stream connected to the server
     * 
     * @param data
     *            contains the move specification, index 0 is the x- coordinate,
     *            index 1 the y- coordinate and index 2 the special number
     */
    public static void sendMove(int[] data) {

        try {
            // split up the message
            dataOut.write(5);
            dataOut.writeInt(5);
            dataOut.writeInt(data[0] * 256 * 256 + data[1]);
            dataOut.write(data[2]);
        } catch (IOException e) {
            // probably got disqualified
            System.out.println("Verbindung wurde abgebrochen");
        }
    }

}
