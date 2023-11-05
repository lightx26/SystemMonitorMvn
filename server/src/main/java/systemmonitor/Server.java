package systemmonitor;

import java.io.*;
import java.net.*;
import java.util.Properties;

import utils.ClientHandler;

public class Server {
    private String HOSTNAME;
    private int PORT;
    private int BACK_LOG;

    private void LoadServerConfig(String fileConfig) {
        Properties config = new Properties();
        try {
            InputStream input = new FileInputStream(new File(fileConfig));
            config.load(input);
            this.HOSTNAME = config.getProperty("HOSTNAME");
            this.PORT = Integer.parseInt(config.getProperty("PORT"));
            this.BACK_LOG = Integer.parseInt(config.getProperty("BACK_LOG"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Run() throws IOException {
        InetAddress address = InetAddress.getByName(this.HOSTNAME);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.PORT, this.BACK_LOG, address);
            serverSocket.setReuseAddress(true);
            System.out.println("Server started at " + this.HOSTNAME + ":" + this.PORT);

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());
                    // Create a thread to handle the client's request
                    Thread clientHandler = new ClientHandler(clientSocket);
                    clientHandler.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server app = new Server();
        app.LoadServerConfig("src\\main\\resources\\config\\config.cfg");
        app.Run();
    }
}