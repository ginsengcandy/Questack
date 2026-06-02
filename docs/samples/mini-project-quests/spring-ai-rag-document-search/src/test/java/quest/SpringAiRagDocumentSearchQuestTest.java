package quest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpringAiRagDocumentSearchQuestTest {

    @Test
    void describesQuestTopic() {
        SpringAiRagDocumentSearchQuest subject = new SpringAiRagDocumentSearchQuest();

        assertThat(subject.describeQuest()).isEqualTo("Spring AI RAG document search");
    }

    @Test
    void learningCriticalLogicIsLeftForTheStudent() {
        // TODO-STUDENT: implement-rag-context-assembly
        // Fill this test with the expected behavior before implementing the TODO-STUDENT code.
        assertThat("TODO-STUDENT: implement-rag-context-assembly").startsWith("TODO-STUDENT");
    }
}
