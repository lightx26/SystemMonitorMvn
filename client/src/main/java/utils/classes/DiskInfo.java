package utils.classes;

public class DiskInfo {
    public String PartitionName;
    public Long TotalSpace;
    public Long FreeSpace;
    public Long UsageSpace;

    public DiskInfo(String partitionName, Long TotalSpace, Long FreeSpace) {
        this.PartitionName = partitionName;
        this.TotalSpace = TotalSpace;
        this.FreeSpace = FreeSpace;
        this.UsageSpace = this.TotalSpace - this.FreeSpace;
    }
}
