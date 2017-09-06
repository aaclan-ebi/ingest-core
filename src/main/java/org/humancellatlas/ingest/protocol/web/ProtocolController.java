package org.humancellatlas.ingest.protocol.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolService;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
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
 * @date 05/09/17
 */
@RepositoryRestController
@ExposesResourceFor(Protocol.class)
@RequiredArgsConstructor
@Getter
public class ProtocolController {
    private final @NonNull ProtocolService protocolService;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols", method = RequestMethod.POST)
    HttpEntity<Protocol> addProtocolToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                               @RequestBody Protocol protocol) {
        Protocol entity = getProtocolService().addProtocolToSubmissionEnvelope(submissionEnvelope, protocol);
        return ResponseEntity.accepted().body(entity);
    }
}