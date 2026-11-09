package com.example.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class RagConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File("data/vector-store.json");
        if (file.exists()) {
            store.load(file);
        }
        return store;
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore) {
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(4).build())
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem("""
            You answer questions using the provided context.
            The context is always correct and up to date.
            If the answer is in the context, state it directly and briefly.
            Do not add disclaimers or suggest contacting anyone.
            If the answer is not in the context, say: "I don't have this information in the documents."
            """)
                .defaultAdvisors(qaAdvisor)
                .build();
    }
}
