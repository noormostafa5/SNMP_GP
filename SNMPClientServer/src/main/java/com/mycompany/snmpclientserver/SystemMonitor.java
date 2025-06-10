package com.mycompany.snmpclientserver;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

public class SystemMonitor {
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem operatingSystem;
<<<<<<< HEAD
    
=======

>>>>>>> origin/Monitor-and-Client-Server
    public SystemMonitor() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.operatingSystem = systemInfo.getOperatingSystem();
    }
<<<<<<< HEAD
    
=======

>>>>>>> origin/Monitor-and-Client-Server
    public double getCpuUsage() {
        CentralProcessor processor = hardware.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
<<<<<<< HEAD
        
        return totalCpu > 0 ? 100.0 * (totalCpu - idle) / totalCpu : 0.0;
    }
    
=======

        return totalCpu > 0 ? 100.0 * (totalCpu - idle) / totalCpu : 0.0;
    }

>>>>>>> origin/Monitor-and-Client-Server
    public double getMemoryUsage() {
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        return 100.0 * (totalMemory - availableMemory) / totalMemory;
    }
<<<<<<< HEAD
    
=======

>>>>>>> origin/Monitor-and-Client-Server
    public String getDiskStatus() {
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        StringBuilder status = new StringBuilder();
<<<<<<< HEAD
        
=======

>>>>>>> origin/Monitor-and-Client-Server
        for (OSFileStore store : fileStores) {
            if (store.getType().contains("Fixed")) {
                long total = store.getTotalSpace();
                long usable = store.getUsableSpace();
                double usagePercent = 100.0 * (total - usable) / total;
<<<<<<< HEAD
                
                status.append(String.format("Mount: %s, Total: %.2f GB, Used: %.2f GB (%.2f%%)\n",
                    store.getMount(),
                    total / (1024.0 * 1024.0 * 1024.0),
                    (total - usable) / (1024.0 * 1024.0 * 1024.0),
                    usagePercent));
            }
        }
        
        return status.toString();
    }
    
=======

                status.append(String.format("Mount: %s, Total: %.2f GB, Used: %.2f GB (%.2f%%)\n",
                        store.getMount(),
                        total / (1024.0 * 1024.0 * 1024.0),
                        (total - usable) / (1024.0 * 1024.0 * 1024.0),
                        usagePercent));
            }
        }

        return status.toString();
    }

>>>>>>> origin/Monitor-and-Client-Server
    public List<DiskStatus> getDiskStatuses() {
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        List<DiskStatus> statuses = new ArrayList<>();
<<<<<<< HEAD
        
=======

>>>>>>> origin/Monitor-and-Client-Server
        for (OSFileStore store : fileStores) {
            if (store.getType().contains("Fixed")) {
                long total = store.getTotalSpace();
                long usable = store.getUsableSpace();
                double usagePercent = 100.0 * (total - usable) / total;
<<<<<<< HEAD
                
                statuses.add(new DiskStatus(
                    store.getMount(),
                    total,
                    usable,
                    usagePercent
                ));
            }
        }
        
        return statuses;
    }
    
    public boolean isSystemHealthy() {
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();
        
        if (cpuUsage > 90.0 || memoryUsage > 90.0) {
            return false;
        }
        
=======

                statuses.add(new DiskStatus(
                        store.getMount(),
                        total,
                        usable,
                        usagePercent
                ));
            }
        }

        return statuses;
    }

    public boolean isSystemHealthy() {
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();

        if (cpuUsage > 90.0 || memoryUsage > 90.0) {
            return false;
        }

>>>>>>> origin/Monitor-and-Client-Server
        for (DiskStatus disk : getDiskStatuses()) {
            if (disk.getUsagePercent() > 90.0) {
                return false;
            }
        }
<<<<<<< HEAD
        
        return true;
    }
    
=======

        return true;
    }

>>>>>>> origin/Monitor-and-Client-Server
    public static class DiskStatus {
        private final String mountPoint;
        private final long totalSpace;
        private final long usableSpace;
        private final double usagePercent;
<<<<<<< HEAD
        
=======

>>>>>>> origin/Monitor-and-Client-Server
        public DiskStatus(String mountPoint, long totalSpace, long usableSpace, double usagePercent) {
            this.mountPoint = mountPoint;
            this.totalSpace = totalSpace;
            this.usableSpace = usableSpace;
            this.usagePercent = usagePercent;
        }
<<<<<<< HEAD
        
        public String getMountPoint() {
            return mountPoint;
        }
        
        public long getTotalSpace() {
            return totalSpace;
        }
        
        public long getUsableSpace() {
            return usableSpace;
        }
        
=======

        public String getMountPoint() {
            return mountPoint;
        }

        public long getTotalSpace() {
            return totalSpace;
        }

        public long getUsableSpace() {
            return usableSpace;
        }

>>>>>>> origin/Monitor-and-Client-Server
        public double getUsagePercent() {
            return usagePercent;
        }
    }
} 