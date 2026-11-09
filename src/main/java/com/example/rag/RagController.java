package com.example.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RagController {

    private final IngestionService ingestionService;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(IngestionService ingestionService, ChatClient chatClient, VectorStore vectorStore) {
        this.ingestionService = ingestionService;
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/documents")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        ingestionService.ingest(file);
        return ResponseEntity.ok("Ingested: " + file.getOriginalFilename());
    }

    @GetMapping("/ask")
    public String ask(@RequestParam("q") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @GetMapping("/debug/search")
    public List<String> debugSearch(@RequestParam("q") String question) {
        SearchRequest request = SearchRequest.builder().query(question).topK(4).build();
        return vectorStore.similaritySearch(request).stream()
                .map(Document::getText)
                .toList();
    }
}
