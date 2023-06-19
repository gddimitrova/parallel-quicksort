package network.programming.server;

import network.programming.quicksort.ParallelQuicksort;
import network.programming.quicksort.Quicksort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class Server {
    private static final int SERVER_PORT = 151552;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 512;

    private final ByteBuffer buffer;

    public Server() {
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }
    private String getClientInput() {
        return new String(buffer.array(), 0, buffer.limit()).trim();
    }

    private String executeQuickSort(List<Double> array) {

        Quicksort algorithm = new Quicksort(array);
        long now = System.nanoTime();
        algorithm.quickSort(0, array.size() - 1);
        long after = System.nanoTime() - now;

        System.out.println("Standard quicksort executed for " + (after) + " nanoseconds");


        ParallelQuicksort parallelQuicksort = new ParallelQuicksort(array, 0, array.size() - 1);
        now = System.nanoTime();
        parallelQuicksort.start();
        after = System.nanoTime() - now;


        try {
            parallelQuicksort.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Parallel quicksort executed for " + (after) + " nanoseconds");

        DecimalFormat decimal = new DecimalFormat("0.#");

        return parallelQuicksort.getNumbers().stream().map(decimal::format)
                .collect(Collectors.joining(", "));
    }

    private List<Double> getArrayFromString(String input) {
        return Arrays.stream(input.strip().split("[\\s, ]+"))
                .filter(p-> p.matches("^\\d+(.\\d+)?$"))
                .map(Double::parseDouble)
                .toList();
    }

    public void startServer() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("New Server is created!");

            while (true) {
                try {
                    int readyChannels = selector.select();

                    if (readyChannels == 0) {
                        continue;
                    }


                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel clientAccepted = server.accept();
                            clientAccepted.configureBlocking(false);
                            clientAccepted.register(selector, SelectionKey.OP_READ);

                            System.out.println("Connection accepted for client " + clientAccepted.getRemoteAddress());

                        } else if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();

                            buffer.clear();

                            int cntBytes = client.read(buffer);
                            if (cntBytes <= 0) {
                                String message = "Nothing to read from client " + client.getRemoteAddress()
                                                    + ", closing channel.";
                                System.out.println(message);
                                client.close();
                                continue;
                            }

                            buffer.flip();
                            String input = getClientInput();

                            List<Double> array = getArrayFromString(input);
                            String output = executeQuickSort(array);


                            output += System.lineSeparator();
                            buffer.clear();
                            buffer.put(output.getBytes());
                            buffer.flip();
                            client.write(buffer);

                        }
                        keyIterator.remove();
                    }

                } catch (IOException e) {
                    System.out.println("Error occurred: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }

    }

    public static void main(String[] args) {
        new Server().startServer();
    }
}
