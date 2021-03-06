package org.humancellatlas.ingest.file;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.query.MetadataCriteria;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class FileService {
    private final @NonNull
    SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull
    FileRepository fileRepository;
    private final @NonNull
    BiomaterialRepository biomaterialRepository;
    private final @NonNull
    ProcessRepository processRepository;
    private final @NonNull
    MetadataDocumentEventHandler metadataDocumentEventHandler;
    private final @NonNull
    MetadataCrudService metadataCrudService;
    private final @NonNull
    MetadataUpdateService metadataUpdateService;

    // TODO Refactor!!!
    public File createFile(String fileName, File file, SubmissionEnvelope submissionEnvelope) {
        if (!fileRepository.findBySubmissionEnvelopeAndFileName(submissionEnvelope, fileName).isEmpty()) {
            throw new FileAlreadyExistsException(String.format("File with name %s already exists in envelope %s", fileName, submissionEnvelope.getId()),
                    fileName);
        } else {
            file.setFileName(fileName);
            file.setUuid(Uuid.newUuid());
            File createdFile = addFileToSubmissionEnvelope(submissionEnvelope, file);
            return createdFile;
        }
    }

    public File addFileToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, File file) {
        File createdFile;
        if (!file.getIsUpdate()) {
            createdFile = metadataCrudService.addToSubmissionEnvelopeAndSave(file, submissionEnvelope);
        } else {
            createdFile = metadataUpdateService.acceptUpdate(file, submissionEnvelope);
        }
        metadataDocumentEventHandler.handleMetadataDocumentCreate(createdFile);
        return createdFile;
    }

    public File addFileValidationJob(File file, ValidationJob validationJob) {
        if (file.getChecksums().getSha1().equals(validationJob.getChecksums().getSha1())) {
            file.setValidationJob(validationJob);
            return fileRepository.save(file);
        } else {
            throw new IllegalStateException(String.format("Failed to create validation job for file with ID %s : checksums mismatch", file.getId()));
        }
    }

    public File updateStagedFile(String envelopeUuid, String fileName, String newFileUrl, Checksums checksums, Long size, String contentType) throws CoreEntityNotFoundException {
        Optional<SubmissionEnvelope> envelope = Optional.ofNullable(submissionEnvelopeRepository.findByUuid(new Uuid(envelopeUuid)));

        if (envelope.isPresent()) {
            List<File> filesInEnvelope = fileRepository.findBySubmissionEnvelopeAndFileName(envelope.get(), fileName);

            if (filesInEnvelope.size() != 1) {
                throw new RuntimeException(String.format("Expected 1 file with name %s, but found %s", fileName, filesInEnvelope.size()));
            } else {
                File file = filesInEnvelope.get(0);
                file.setCloudUrl(newFileUrl);
                file.setChecksums(checksums);
                file.setSize(size);
                file.setFileContentType(contentType);
                file.enactStateTransition(ValidationState.DRAFT);
                File updatedFile = fileRepository.save(file);
                return updatedFile;
            }
        } else {
            // todo log
            throw new CoreEntityNotFoundException(String.format("Couldn't find envelope with with uuid %s", envelopeUuid));
        }
    }

}
