package com.example.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class IngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    public void ingest(MultipartFile file) {
        String filename = file.getOriginalFilename();

        InputStreamResource resource;
        try {
            resource = new InputStreamResource(file.getInputStream()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        documents.forEach(document -> {
            assert filename != null;
            document.getMetadata().put("source", filename);
        });

        List<Document> chunks = textSplitter.apply(documents);
        vectorStore.add(chunks);
    }
}
