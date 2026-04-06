package org.example.benchmark;

import org.example.db.datasource.PooledDataSourceFactory;
import org.example.db.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ConnectionBenchmark {
    private ConnectionBenchmark() {}

    public static long runSingleConnection(DataSource dataSource, int threadCount, int sleepSeconds) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Connection connection = dataSource.getConnection();
                    synchronized (connection) {
                        try (PreparedStatement ps = connection.prepareStatement("SELECT pg_sleep(?)")) {
                            ps.setInt(1, sleepSeconds);
                            ps.execute();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        return System.currentTimeMillis() - start;
    }

    public static long runPooled(DataSource dataSource, int threadCount, int sleepSeconds) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement ps = connection.prepareStatement("SELECT pg_sleep(?)")) {
                    ps.setInt(1, sleepSeconds);
                    ps.execute();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/greenfleet";
        String user = "greenfleet_user";
        String pass = "greenfleet_pass";

        int threads = 10;
        int sleepSeconds = 2;

        DataSource single = new SingleConnectionDataSource(url, user, pass);
        long singleMs = runSingleConnection(single, threads, sleepSeconds);

        DataSource pooled = PooledDataSourceFactory.createHikari(url, user, pass, 5);
        long pooledMs = runPooled(pooled, threads, sleepSeconds);

        System.out.println("==== Connection Benchmark ====");
        System.out.println("Threads       : " + threads);
        System.out.println("Sleep seconds : " + sleepSeconds);
        System.out.println("Single DS ms  : " + singleMs);
        System.out.println("Pooled DS ms  : " + pooledMs);
    }
}
