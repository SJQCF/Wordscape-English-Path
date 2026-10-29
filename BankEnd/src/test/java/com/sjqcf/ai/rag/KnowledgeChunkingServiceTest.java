package com.sjqcf.ai.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeChunkingServiceTest {

    @Test
    void shouldSplitCourseFaqAndKeepMetadata() {
        RagChunkProperties properties = new RagChunkProperties();
        properties.setChunkSize(80);
        properties.setMinChunkSizeChars(40);
        properties.setMinChunkLengthToEmbed(10);

        KnowledgeChunkingService service = new KnowledgeChunkingService(properties);
        List<Document> chunks = service.splitText(sampleCourseFaq(), Map.of(
                "courseId", "business-english-101",
                "docType", "faq",
                "title", "Course service FAQ"
        ));

        assertTrue(chunks.size() > 1, "The sample should produce more than one chunk");
        assertEquals(chunks.size(), chunks.get(0).getMetadata().get("chunkCount"));
        assertFalse(chunks.get(0).getId().isBlank());

        for (int index = 0; index < chunks.size(); index++) {
            Document chunk = chunks.get(index);
            assertEquals(index, chunk.getMetadata().get("chunkIndex"));
            assertEquals("business-english-101", chunk.getMetadata().get("courseId"));
            assertEquals("faq", chunk.getMetadata().get("docType"));
            assertEquals(chunks.get(0).getMetadata().get("sourceDocumentId"),
                    chunk.getMetadata().get("sourceDocumentId"));
            assertFalse(chunk.getText().isBlank());
            System.out.printf("[%d/%d] %s%n", index + 1, chunks.size(), chunk.getText());
        }
    }

    private String sampleCourseFaq() {
        return """
                Course service FAQ

                Refund policy: A student can request a full refund within seven days after enrollment.
                After seven days, the refund amount depends on the number of completed lessons.
                If a live class has already started, the class fee is not refundable.

                Schedule policy: Students can reschedule a private lesson at least twelve hours before the lesson starts.
                Late rescheduling may be treated as a completed lesson.
                Group classes cannot be rescheduled individually, but students can watch the replay.

                Course access: Recorded lessons remain available for one year after the course starts.
                Course materials are for personal study only and cannot be shared with other people.
                If a student changes to another course, the original course access ends after the transfer is approved.

                Payment policy: The price shown on the course page includes the course fee and platform service fee.
                Students should not pay any teacher directly outside the platform.
                If a payment is unsuccessful, the platform will release the order after thirty minutes.

                Support policy: For account or payment problems, contact customer service with the order number.
                For learning problems, students can ask the course assistant or book a tutoring session.
                Urgent technical issues are handled within one business day.
                """;
    }
}
