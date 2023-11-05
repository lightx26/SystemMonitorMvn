package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import utils.classes.DiskInfo;

import java.util.ArrayList;

public class SystemInfo {
    Integer mb = 1024 * 1024; // MB
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public String osName() {
        return System.getProperty("os.name");
    }

    public String osVersion() {
        return System.getProperty("os.version");
    }

    public String osArch() {
        return System.getProperty("os.arch");
    }

    public String getCpuModel() {
        try {
            Process p = Runtime.getRuntime().exec("wmic cpu get name");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            input.readLine();
            input.readLine();
            String CpuName = input.readLine();
            input.close();
            return CpuName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public ArrayList<DiskInfo> diskInfo() {
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        ArrayList<DiskInfo> diskInfos = new ArrayList<>();
        /* For each filesystem root, print some info */
        for (File root : roots) {
            diskInfos.add(new DiskInfo(root.getAbsolutePath(), root.getTotalSpace() / mb, root.getFreeSpace() / mb));
        }
        return diskInfos;
    }

    public double getCpuLoad() {

        // What % load the overall system is at, from 0.0-1.0
        return osBean.getSystemCpuLoad() * 100;
    }

    public Long getFreeMemory() {
        return osBean.getFreeMemorySize() / mb;
    }

    public Long getTotalMem() {
        return osBean.getTotalMemorySize() / mb;
    }

    public Long getMemUsage() {
        return (this.getTotalMem() - this.getFreeMemory());
    }
}