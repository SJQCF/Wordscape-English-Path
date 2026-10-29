package com.sjqcf.ai.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KnowledgeChunkingService {

    private final TokenTextSplitter splitter;

    public KnowledgeChunkingService(RagChunkProperties properties) {
        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(properties.getChunkSize())
                .withMinChunkSizeChars(properties.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(properties.getMinChunkLengthToEmbed())
                .withMaxNumChunks(properties.getMaxNumChunks())
                .withKeepSeparator(properties.isKeepSeparator())
                .build();
    }

    public List<Document> splitText(String text, Map<String, Object> metadata) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Document source = Document.builder()
                .id(UUID.randomUUID().toString())
                .text(text)
                .metadata(safeMetadata(metadata))
                .build();
        return split(source);
    }

    public List<Document> split(Document source) {
        if (source == null || source.getText() == null || source.getText().isBlank()) {
            return List.of();
        }

        String sourceId = source.getId() == null || source.getId().isBlank()
                ? UUID.randomUUID().toString()
                : source.getId();
        Document normalized = Document.builder()
                .id(sourceId)
                .text(source.getText())
                .metadata(safeMetadata(source.getMetadata()))
                .build();

        List<Document> rawChunks = splitter.split(normalized);
        List<Document> chunks = new ArrayList<>(rawChunks.size());
        for (int index = 0; index < rawChunks.size(); index++) {
            Document rawChunk = rawChunks.get(index);
            Map<String, Object> chunkMetadata = new HashMap<>(safeMetadata(rawChunk.getMetadata()));
            chunkMetadata.put("sourceDocumentId", sourceId);
            chunkMetadata.put("chunkIndex", index);
            chunkMetadata.put("chunkCount", rawChunks.size());
            chunkMetadata.put("charLength", rawChunk.getText().length());

            chunks.add(Document.builder()
                    .id(sourceId + "-" + String.format("%04d", index))
                    .text(rawChunk.getText())
                    .metadata(chunkMetadata)
                    .build());
        }
        return chunks;
    }

    private Map<String, Object> safeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new HashMap<>();
        metadata.forEach((key, value) -> {
            if (key != null && value != null) {
                result.put(key, value);
            }
        });
        return result;
    }
}
