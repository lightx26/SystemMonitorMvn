package utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

import utils.classes.DiskInfo;

import java.io.File;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private String MAC = null;
    private String OSName = null;
    private String CPUModel = null;

    DataAccess dataAccess;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            dataAccess = new DataAccess();

            MAC = GetMACAddress();
            OSName = GetOSName();
            CPUModel = GetCPUModel();
            receiveObject();
            // receiveFile();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String GetOSName() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            String OSName = dis.readUTF();
            return OSName;
        } catch (Exception e) {
            System.out.println("The system can't get the CPU Model!");
            return "";
        }
    }

    private String GetCPUModel() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            String GPUModel = dis.readUTF();
            return GPUModel;
        } catch (Exception e) {
            System.out.println("The system can't get the CPU Model!");
            return "";
        }
    }

    private String GetMACAddress() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            String IP = clientSocket.getInetAddress().getHostAddress();
            dos.writeUTF(IP);
            String MAC = dis.readUTF();
            return MAC;
        } catch (Exception e) {
            System.out.println("The system can't get the MAC address!");
            return "";
        }
    }

    private ArrayList<String> Bytes2ArrayList(byte[] b) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(bais);
            ArrayList<String> data = (ArrayList) ois.readObject();
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    private void receiveObject() throws Exception {
        while (this.clientSocket.isConnected()) {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

            Double CPULoad = dis.readDouble();
            Long MemUsage = dis.readLong();
            Long TotalMem = dis.readLong();

            int diskLen = dis.readInt();
            ArrayList<DiskInfo> diskInfos = new ArrayList<>();
            for (int i = 0; i < diskLen; i++) {
                String[] d = dis.readUTF().split(",");
                diskInfos.add(new DiskInfo(d[0], Long.parseLong(d[1]), Long.parseLong(d[2])));
            }

            int length = dis.readInt();
            byte[] data = new byte[length];
            if (length > 0) {
                dis.readFully(data, 0, length);
            }

            ArrayList<String> processes = Bytes2ArrayList(data);

            System.out.println("=========");
            System.out.println("OS: " + OSName + "\nCPU Model: " + CPUModel);
            System.out.println("CPU Load: " + CPULoad);
            System.out.println("Mem: " + MemUsage + "/" + TotalMem + "MB");
            System.out.println("Disks: ");
            for (DiskInfo d : diskInfos) {
                System.out.println(d.PartitionName + " # Disk Space: " + d.UsageSpace + "/" + d.TotalSpace + "MB");
            }

            System.out.println("MAC: " + MAC + ": " + processes.size());

            dataAccess.addCpuUsage(CPULoad);
            dataAccess.addMemUsage(MemUsage);

            ArrayList<Double> cpus = dataAccess.getCpuUsages();
            ArrayList<Long> mems = dataAccess.getMemoryUsages();

            for (Long element : mems) {
                System.out.println("Memory use: " + element);
            }

            for (Double element : cpus) {
                System.out.println("CPU use: " + element);
            }
            // int lim = 10;
            // // if (cpu_queue.size() < lim) {
            // // cpu_queue.add(CPULoad);
            // // } else {
            // // cpu_queue.poll();
            // // cpu_queue.add(CPULoad);
            // // }

            // if (db.llen("CPU") >= lim) {
            // db.lpop("CPU");
            // }
            // db.rpush("CPU", CPULoad.toString());

            // // db.del("CPU");
            // // for (Double _cpu : cpu_queue) {
            // // db.rpush("CPU", _cpu.toString());
            // // }

            // // for (String p : processes) {
            // // System.out.println(p);
            // // }

            // List<String> cpus = db.lrange("CPU", 0, 9);

            // // Process the retrieved elements
            // for (String element : cpus) {
            // System.out.println("CPU: " + element + "%");
            // }

            System.out.println("=========");
        }
    }

    private void receiveFile() {
        try {
            // Input stream to receive data from the client
            InputStream inputStream = clientSocket.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            // Read the file name and size from the client
            // dataInputStream.readUTF();

            String fileName = "filename.txt";
            dataInputStream.readLong();

            System.out.println("Receiving file: " + fileName);

            // Output stream to save the received file
            String filePath = "downloads\\" + fileName;

            // Check extention of file
            String fileExtention = filePath.substring(filePath.lastIndexOf('.') + 1);
            // then write file to server's disk
            if (fileExtention.equals("txt"))
                writeTextfile(filePath, dataInputStream);
            else
                writeBinaryfile(filePath, dataInputStream);

            // Close socket

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTextfile(String filePath, DataInputStream dataInputStream) {
        try {
            InputStreamReader in = new InputStreamReader(dataInputStream);
            BufferedReader reader = new BufferedReader(in);
            FileWriter fileWriter = new FileWriter(new File(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.write(line);
                fileWriter.write("\n");
            }

            // System.out.println("File received successfully.\nSize: " + totalBytesReceived
            // + " bytes");

            // Close streams
            fileWriter.close();
            in.close();
            dataInputStream.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBinaryfile(String filePath, DataInputStream dataInputStream) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytesReceived = 0;

            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesReceived += bytesRead;
                // fileSize -= bytesRead;
            }

            System.out.println("File received successfully.\nSize: " + totalBytesReceived + " bytes");

            // Close streams
            fileOutputStream.close();
            dataInputStream.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
