package network.programming.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private static final int SERVER_PORT = 151552;
    private static final String SERVER_HOST = "localhost";

    public void startClient() {

        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, "UTF-8"));
             PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, "UTF-8"), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");


            while (true) {
                System.out.print("Enter array of numbers: ");
                String message = scanner.nextLine();

                if ("quit".equals(message)) {
                    break;
                }

                System.out.println("Sending array [" + message + "] to the server.");

                writer.println(message);

                String line;
                if ((line = reader.readLine()) != null) {
                    System.out.println("The server replied [" + line + "].");
                }

            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the client channel", e);
        }
    }

    public static void main(String[] args) {
        new Client().startClient();
    }
}