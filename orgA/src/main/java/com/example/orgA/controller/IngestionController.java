//package com.example.orgA.controller;
//
//import com.example.orgA.dto.EventDto;
//import com.example.orgA.service.IngestionService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.codec.ServerSentEvent;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//
//import java.util.Map;
//
//@Tag(name = "Ingestion API", description = "Manage Ingestion Tasks")
//@RestController
//@RequestMapping("/ingest")
//public class IngestionController {
//
//    private final IngestionService ingestionService;
//    public IngestionController(IngestionService ingestionService) {
//        this.ingestionService = ingestionService;
//    }
//
//    @Operation(summary = "Start Ingestion Stream", description = "Stream real-time ingestion events via SSE")
//    @GetMapping(path = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<EventDto>> streamIngestion() {
//        return ingestionService
//                .startIngestionFlux()
//                .map(event -> {
//                    EventDto dto = EventDto.from(event);
//                    return ServerSentEvent.<EventDto>builder()
//                            .id(dto.caseID())
//                            .event("wiki-change")
//                            .data(dto)
//                            .build();
//                });
//    }
//
//    @Operation(summary = "Stop Ingestion", description = "Stop Ingestion from source.")
//    @PostMapping("/stop")
//    public ResponseEntity<String> stopIngestion() {
//        ingestionService.stopIngestion();
//        return ResponseEntity.ok("Ingestion stopped.");
//    }
//
//    @Operation(summary = "Only Start Ingestion", description = "Only Ingestion from source url.")
//    @PostMapping("/only-ingest")
//    public ResponseEntity<String> onlyIngestion(@RequestBody Map<String, String> request) {
//        String sourceUrl = request.get("url");
//        String sourceId  = request.get("sourceId");
//
//        if ((sourceUrl == null || sourceUrl.isEmpty()) &&
//                (sourceId == null   || sourceId.isEmpty())) {
//            return ResponseEntity.badRequest()
//                    .body("Error: Wikipedia URL or Source ID is required.");
//        }
//
//        ingestionService.onlyIngestion(sourceUrl, sourceId);
//        return ResponseEntity.ok("Ingestion started for URL: " + sourceUrl);
//    }
//}
