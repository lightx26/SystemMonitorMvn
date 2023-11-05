package utils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Sender {
    private Socket clientSocket;

    // Injection FilePath and IP, Port to create Socket
    public Sender(Socket socket) {
        try {
            clientSocket = socket;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send file via JavaSocket
    public void sendFile(String filePath) {
        try {
            if (clientSocket.isConnected()) {
                System.out.println("Connected to server");

                // Get the file name and size
                File fileToSend = new File(filePath);
                // String fileName = fileToSend.getName();
                long fileSize = fileToSend.length();

                // Output stream to send data to the server
                OutputStream outputStream = clientSocket.getOutputStream();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

                // Send file name and size to the server
                // dataOutputStream.writeUTF(fileName);
                dataOutputStream.writeLong(fileSize);

                // Input stream to read the file and send it to the server
                FileInputStream fileInputStream = new FileInputStream(fileToSend);
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                    dataOutputStream.flush();
                }

                System.out.println("File sent successfully.\nSize: " + fileSize + " bytes");

                // Close streams and socket
                fileInputStream.close();
                dataOutputStream.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String cmd) {
        try {
            if (clientSocket.isConnected()) {
                OutputStream outputStream = clientSocket.getOutputStream();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

                dataOutputStream.writeUTF(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            // if (!clientSocket.isClosed())
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
