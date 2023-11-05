package utils;

import java.util.ArrayList;

import redis.clients.jedis.Jedis;

public class DataAccess {
    Jedis jedis;
    static int lim = 10;

    public DataAccess() {
        jedis = new Jedis("192.168.234.131", 6379);
    }

    public ArrayList<Double> getCpuUsages() {
        ArrayList<Double> list = new ArrayList<Double>();

        for (String s_cpu : jedis.lrange("CPU", 0, lim)) {
            list.add(Double.parseDouble(s_cpu));
        }

        return list;
    }

    public void addCpuUsage(Double cpu) {
        if (jedis.llen("CPU") >= lim)
            jedis.lpop("CPU");

        jedis.rpush("CPU", cpu.toString());
    }

    public ArrayList<Long> getMemoryUsages() {
        ArrayList<Long> list = new ArrayList<Long>();

        for (String s_mem : jedis.lrange("Memory", 0, lim)) {
            list.add(Long.parseLong(s_mem));
        }

        return list;
    }

    public void addMemUsage(Long mem) {
        if (jedis.llen("Memory") >= lim)
            jedis.lpop("Memory");

        jedis.rpush("Memory", mem.toString());
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
