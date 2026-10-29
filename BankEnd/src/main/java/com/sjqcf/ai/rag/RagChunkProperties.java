package com.sjqcf.ai.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.rag.chunk")
public class RagChunkProperties {

    /**
     * Target chunk size in tokens, not characters.
     */
    private int chunkSize = 500;

    /**
     * Small trailing chunks are merged into the previous chunk when possible.
     */
    private int minChunkSizeChars = 120;

    /**
     * Chunks shorter than this value are ignored.
     */
    private int minChunkLengthToEmbed = 20;

    /**
     * Safety limit for one source document.
     */
    private int maxNumChunks = 10000;

    /**
     * Keep line separators so headings and paragraphs remain readable.
     */
    private boolean keepSeparator = true;
}
