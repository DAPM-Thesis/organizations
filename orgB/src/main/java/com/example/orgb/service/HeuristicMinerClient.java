package com.example.orgb.service;

import communication.message.impl.event.Event;
import communication.message.serialization.MessageSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Drives a long‐lived heuristics-miner.jar process, restarting it
 * if it dies, and extracts the direct‐follow metrics.
 */
public class HeuristicMinerClient implements Closeable {

    // Immutable once set in ctor
    private final String jarPath;

    // Synchronize all process I/O on this
    private final Object lock = new Object();

    private Process       process;
    private BufferedWriter stdin;
    private BufferedReader stdout;
    private final ObjectMapper mapper = new ObjectMapper();

    public static class DirectFollow {
        public final String from, to;
        public final long   frequency;
        public final double dependency;

        public DirectFollow(String from, String to, long frequency, double dependency) {
            this.from       = from;
            this.to         = to;
            this.frequency  = frequency;
            this.dependency = dependency;
        }

        @Override
        public String toString() {
            return String.format("%s→%s (freq=%d, dep=%.3f)",
                    from, to, frequency, dependency);
        }
    }

    /**
     * @param jarPath absolute path to heuristics-miner.jar
     */
    public HeuristicMinerClient(String jarPath) {
        this.jarPath = jarPath;
    }

    /**
     * Lazily (re)starts the subprocess if it isn't alive.
     */
    private void startProcess() {
        synchronized (lock) {
            try {
                if (process == null || !process.isAlive()) {
                    ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath, "--json-metrics");
                    pb.redirectErrorStream(true);
                    process = pb.start();

                    stdin  = new BufferedWriter(
                            new OutputStreamWriter(
                                    process.getOutputStream(),
                                    StandardCharsets.UTF_8));
                    stdout = new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream(),
                                    StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Could not start heuristic-miner.jar", e);
            }
        }
    }

    /**
     * Send one Event to the miner and get back the direct-follow metrics.
     */
    public List<DirectFollow> mine(Event e) {
        synchronized (lock) {
            try {
                System.out.println("Mining Function!!");
                // 1) Ensure subprocess is running
                startProcess();

                // 2) Serialize the Event to JSON via your MessageSerializer
                MessageSerializer serializer = new MessageSerializer();
                e.acceptVisitor(serializer);
                String jsonIn = serializer.getSerialization();

                // 3) Convert the ISO timestamp into epoch seconds, but keep it QUOTED
                //    so JXESParser.parseEvent sees a String it can Long.parseLong()
                Instant inst = Instant.parse(e.getTimestamp());
                String epochStr = String.valueOf(inst.getEpochSecond());
                jsonIn = jsonIn.replaceAll(
                        "\"date\"\\s*:\\s*\"[^\"]+\"",
                        "\"date\":\"" + epochStr + "\""
                );

                // 4) Send JSON + newline + flush
                stdin.write(jsonIn);
                stdin.newLine();
                stdin.flush();

                // 5) Read JSON payload lines until a blank line
                StringBuilder sb = new StringBuilder();
                long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
                String line;
                while ((line = stdout.readLine()) != null && !line.isEmpty()) {
                    sb.append(line).append('\n');
                    if (System.currentTimeMillis() > deadline) {
                        throw new IOException("Timeout waiting for miner JSON");
                    }
                }

                // 6) Read the single status line ("true"/"false")
                String statusLine = stdout.readLine();
                boolean success = Boolean.parseBoolean(statusLine);

                // 7) Debug‐print what we got
                String jsonOut = sb.toString().trim();
                System.out.println("RAW miner output: " + jsonOut);
                System.out.println("success flag: " + success);

                if (!success) {
                    return List.of();
                }

                // 8) Parse the JSON metrics
                JsonNode root = mapper.readTree(jsonOut);
                JsonNode arcs = root.path("arcs");
                List<DirectFollow> result = new ArrayList<>();
                for (JsonNode arc : arcs) {
                    String from = arc.path("source").asText();
                    String to   = arc.path("target").asText();
                    long   freq = arc.path("frequency").asLong();
                    double dep  = arc.path("dependency").asDouble();
                    result.add(new DirectFollow(from, to, freq, dep));
                }
                return result;

            } catch (IOException ex) {
                // broken pipe or JSON error: next call will restart the jar
                throw new UncheckedIOException("Error talking to heuristic-miner.jar", ex);
            }
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            try {
                if (stdin  != null) stdin.close();
                if (stdout != null) stdout.close();
                if (process!= null) process.destroy();
                if (process!= null) process.waitFor(5, TimeUnit.SECONDS);
            } catch (Exception ignore) { }
        }
    }
}
