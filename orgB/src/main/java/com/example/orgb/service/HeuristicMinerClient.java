package com.example.orgb.service;

import communication.message.impl.event.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.serialization.MessageSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeuristicMinerClient implements Closeable {

    public static class DirectFollow {
        public final String from, to;
        public final long frequency;
        public final double dependency;

        public DirectFollow(String from, String to, long freq, double dep) {
            this.from       = from;
            this.to         = to;
            this.frequency  = freq;
            this.dependency = dep;
        }

        @Override
        public String toString() {
            return String.format("%s→%s (freq=%d, dep=%.3f)",
                    from, to, frequency, dependency);
        }
    }

    private final Process       process;
    private final BufferedWriter stdin;
    private final BufferedReader stdout;
    private final ObjectMapper  mapper = new ObjectMapper();

    public HeuristicMinerClient(String jarPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath);
            // merge stderr into stdout so we see errors inline
            pb.redirectErrorStream(true);
            this.process = pb.start();

            this.stdin  = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
            this.stdout = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"));

        } catch (IOException e) {
            throw new UncheckedIOException("Could not start heuristic-miner.jar", e);
        }
    }

    /**
     * Send one Event to the miner and get back the direct-follow metrics.
     */
    public List<DirectFollow> mine(Event e) {
        try {
            // 1) serialize event
            MessageSerializer serializer = new MessageSerializer();
            e.acceptVisitor(serializer);                           // ← drive the serializer
            String jsonIn = serializer.getSerialization();

            // 2) send JSON + newline
            stdin.write(jsonIn);
            stdin.newLine();
            stdin.flush();

            // 3) read until blank line
            StringBuilder sb = new StringBuilder();
            long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
            String line;
            while ((line = stdout.readLine()) != null && !line.isEmpty()) {
                sb.append(line).append('\n');
                if (System.currentTimeMillis() > deadline) {
                    throw new IOException("Timeout from miner");
                }
            }

            // next line is "true" or "false"
            boolean success = Boolean.parseBoolean(stdout.readLine());
            if (!success) {
                return List.of();
            }

            // 4) parse JSON
            JsonNode root = mapper.readTree(sb.toString());
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
            throw new UncheckedIOException("Error talking to heuristic-miner.jar", ex);
        }
    }

    @Override
    public void close() {
        try {
            stdin.close();
            stdout.close();
            process.destroy();
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            // log or rethrow if you want
        }
    }
}
