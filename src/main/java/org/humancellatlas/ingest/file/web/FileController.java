package org.humancellatlas.ingest.file.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@RepositoryRestController
@ExposesResourceFor(File.class)
@RequiredArgsConstructor
@Getter

public class FileController {

    @NonNull
    private final FileService fileService;

    @NonNull
    private final ProcessRepository processRepository;

    @NonNull
    private final PagedResourcesAssembler pagedResourcesAssembler;


    @RequestMapping(path = "/files/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingFile(@PathVariable("id") File file, final PersistentEntityResourceAssembler assembler) {
        file.setValidationState(ValidationState.VALIDATING);
        file = getFileService().getFileRepository().save(file);
        return ResponseEntity.accepted().body(assembler.toFullResource(file));
    }

    @RequestMapping(path = "/files/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateFile(@PathVariable("id") File file, final PersistentEntityResourceAssembler assembler) {
        file.setValidationState(ValidationState.VALID);
        file = getFileService().getFileRepository().save(file);
        return ResponseEntity.accepted().body(assembler.toFullResource(file));
    }

    @RequestMapping(path = "/files/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateFile(@PathVariable("id") File file, final PersistentEntityResourceAssembler assembler) {
        file.setValidationState(ValidationState.INVALID);
        file = getFileService().getFileRepository().save(file);
        return ResponseEntity.accepted().body(assembler.toFullResource(file));
    }

    @RequestMapping(path = "/files/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingFile(@PathVariable("id") File file, final PersistentEntityResourceAssembler assembler) {
        file.setValidationState(ValidationState.PROCESSING);
        file = getFileService().getFileRepository().save(file);
        return ResponseEntity.accepted().body(assembler.toFullResource(file));
    }

    @RequestMapping(path = "/files/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeFile(@PathVariable("id") File file, final PersistentEntityResourceAssembler assembler) {
        file.setValidationState(ValidationState.COMPLETE);
        file = getFileService().getFileRepository().save(file);
        return ResponseEntity.accepted().body(assembler.toFullResource(file));
    }
}
