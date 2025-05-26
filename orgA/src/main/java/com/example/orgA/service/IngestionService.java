//// src/main/java/com/example.orgA/service/IngestionService.java
//package com.example.orgA.service;
//
//import com.example.orgA.preProcessingElements.streamSources.SSEStreamSource;
//import communication.message.impl.event.Event;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Sinks;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Service
//public class IngestionService {
//    private final Sinks.Many<Event> sink =
//            Sinks.many().multicast().onBackpressureBuffer();
//    private final ExecutorService executor =
//            Executors.newSingleThreadExecutor();
//    private volatile boolean running = false;
//
//    // Hold onto the SSE source so we can stop it later
//    private SSEStreamSource streamSource;
//
//    public IngestionService() {
//        // KafkaProducerService has been removed
//    }
//
//    /**
//     * Starts the SSE ingestion stream (only once) and returns a Flux of Events.
//     */
//    public synchronized Flux<Event> startIngestionFlux() {
//        if (!running) {
//            running = true;
//            streamSource = new SSEStreamSource();
//            executor.submit(this::runStreamSource);
//        }
//        return sink.asFlux();
//    }
//
//    /**
//     * Internal loop: pulls from the SSEStreamSource while running.
//     */
//    private void runStreamSource() {
//        streamSource.start();
//        Event e;
//        while (running && (e = streamSource.process()) != null) {
//            sink.tryEmitNext(e);
//        }
//    }
//
//    /**
//     * Stops the SSE ingestion: flips the running flag,
//     * closes the source, shuts down the executor, and completes the Flux.
//     */
//    public synchronized void stopIngestion() {
//        if (running) {
//            running = false;
//            if (streamSource != null) {
//                streamSource.stop();
//            }
//            executor.shutdownNow();
//            sink.tryEmitComplete();
//        }
//    }
//
//    /**
//     * A one-off ingestion from a custom URL/sourceId; retains the same pattern
//     * if you later want to extend stop() support here.
//     */
//    public synchronized void onlyIngestion(String url, String sourceId) {
////        SSEStreamSource src = new SSEStreamSource(url, sourceId);
////        src.start();
//        // keep a reference if you need to call src.stop() later
//    }
//}
