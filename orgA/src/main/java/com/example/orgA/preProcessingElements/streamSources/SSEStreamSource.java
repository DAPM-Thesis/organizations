//package com.example.orgA.preProcessingElements.streamSources;
//
//import com.example.orgA.config.SpringContext;
//import com.example.orgA.mongo.AnonymizationMappingService;
//import com.example.orgA.preProcessingElements.AnonymizationProcess;
//import com.example.orgA.preProcessingElements.AttributeSettingProcess;
//import com.example.orgA.preProcessingElements.FiltrationProcess;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.launchdarkly.eventsource.EventHandler;
//import com.launchdarkly.eventsource.EventSource;
//import com.launchdarkly.eventsource.MessageEvent;
//import communication.message.impl.event.Event;
//import pipeline.processingelement.source.SimpleSource;
//
//import java.net.URI;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
///**
// * SSE-based source that:
// * 1) Filters incoming JSON
// * 2) Anonymizes sensitive fields
// * 3) Dynamically extracts attributes into Event objects
// *
// * Now with error-handling around extraction so you can see exactly where
// * NullPointerExceptions or others occur.
// */
//public class SSEStreamSource extends SimpleSource<Event> {
//
//    private static final String SSE_URL      = "https://stream.wikimedia.org/v2/stream/recentchange";
//    private static final long   FILTERING_ID = 2L;
//    private static final long   ATTRIBUTE_ID = 2L;
//    private static final String SOURCE_ID    = "wiki-edit";
//
//    private final BlockingQueue<Event>         eventQueue;
//    private final EventSource                  eventSource;
//    private final ObjectMapper                 mapper;
//    private final FiltrationProcess            filtrationProcess;
//    private final AttributeSettingProcess      attributeProcess;
//    private final AnonymizationProcess         anonymizationProcess;
//    private final AnonymizationMappingService  anonymizationMappingService;
//
//    public SSEStreamSource() {
//        this.eventQueue               = new LinkedBlockingQueue<>();
//        this.mapper                   = new ObjectMapper();
//        this.filtrationProcess        = FiltrationProcess.fromFilterId(FILTERING_ID);
//        this.attributeProcess         = AttributeSettingProcess.fromSettingId(ATTRIBUTE_ID);
//        this.anonymizationProcess     = AnonymizationProcess.fromDataSourceId(SOURCE_ID);
//        this.anonymizationMappingService = SpringContext
//                .getBean(AnonymizationMappingService.class);
//
//        EventHandler handler = new EventHandler() {
//            @Override public void onOpen() {}
//            @Override public void onClosed() {}
//            @Override public void onComment(String comment) {}
//
//            @Override
//            public void onError(Throwable t) {
//                System.err.println("Ingestion-Service Error (SSE handler): " + t.getMessage());
//                t.printStackTrace();
//            }
//
//            @Override
//            public void onMessage(String event, MessageEvent messageEvent) {
//                try {
//                    // 0) parse raw JSON
//                    JsonNode json = mapper.readTree(messageEvent.getData());
//
//                    // Optional: store raw payload for auditing
//                    anonymizationMappingService.saveRawData(SOURCE_ID, json);
//
//                    // 1) filtration
//                    if (!filtrationProcess.shouldPass(json)) {
//                        return;
//                    }
//
//                    // 2) anonymization
//                    json = anonymizationProcess.apply(json);
//
//                    // 3) attribute-setting → may throw NPE if config/path is wrong
//                    Event dapmEvent = attributeProcess.extractEvent(json);
//
//                    // 4) enqueue for downstream
//                    eventQueue.put(dapmEvent);
//                    System.out.println("Ingested Value!! ");
//
//                } catch (NullPointerException npe) {
//                    // catch the exact NPE you're seeing
//                    System.err.println("NullPointerException in onMessage(): " + npe.getMessage());
//                    System.err.println("Offending payload: " + messageEvent.getData());
//                    npe.printStackTrace();
//
//                } catch (Exception e) {
//                    // catch any other parsing/processing errors
//                    System.err.println("Error processing messageEvent: " + e.getMessage());
//                    System.err.println("Payload: " + messageEvent.getData());
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        this.eventSource = new EventSource.Builder(handler, URI.create(SSE_URL)).build();
//    }
//
//    @Override
//    public Event process() {
//        try {
//            return eventQueue.take();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return null;
//        }
//    }
//
//    @Override
//    public void start() {
//        eventSource.start();
//    }
//
//    public void stop() {
//        eventSource.close();
//    }
//}
