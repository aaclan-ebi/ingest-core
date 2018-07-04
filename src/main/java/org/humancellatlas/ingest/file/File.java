package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

@Getter
@Setter
public class File extends MetadataDocument {

    @RestResource @DBRef private final List<Process> inputToProcesses = new ArrayList<>();

    @RestResource @DBRef private final List<Process> derivedByProcesses = new ArrayList<>();

    private String fileName;
    private String cloudUrl;
    private Checksums checksums;
    private UUID validationId;
    private UUID dataFileUuid;

    public File(){
        super(EntityType.FILE, null);
        setDataFileUuid(UUID.randomUUID());
    }

    public File(@JsonProperty("content") Object content) {
        super(EntityType.FILE, content);
    }

    public File addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        super.addToSubmissionEnvelope(submissionEnvelope);

        return this;
    }

    /**
     * Adds to the collection of processes that this file serves as an input to
     *
     * @param process the process to add
     * @return a reference to this file
     */
    public File addAsInputToProcess(Process process) {
        this.inputToProcesses.add(process);

        return this;
    }

    /**
     * Adds to the collection of processes that this file was derived by
     *
     * @param process the process to add
     * @return a reference to this file
     */
    public File addAsDerivedByProcess(Process process) {
        String processId = process.getId();
        boolean processInList = derivedByProcesses.stream()
                .map(Process::getId)
                .anyMatch(processId::equals);
        if (!processInList) {
            this.derivedByProcesses.add(process);
        }
        return this;
    }

    public void addToAnalysis(Process analysis) {
        //TODO check if this File and the Analysis belong to the same Submission?
        List<SubmissionEnvelope> submissionEnvelopes = getSubmissionEnvelopes();
        if (submissionEnvelopes == null || submissionEnvelopes.isEmpty()) {
            SubmissionEnvelope submissionEnvelope = analysis.getOpenSubmissionEnvelope();
            addToSubmissionEnvelope(submissionEnvelope);
        }
        addAsDerivedByProcess(analysis);
    }

}
