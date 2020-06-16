package org.humancellatlas.ingest.submission.submissiondestination.strategy.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.humancellatlas.ingest.submission.submissiondestination.strategy.SubmissionStrategy;

@RequiredArgsConstructor
public class JustArchive implements SubmissionStrategy {
  private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;

  @Override
  public void handleState(SubmissionEnvelope submissionEnvelope, SubmissionState currentState) {
    if(currentState.equals(SubmissionState.SUBMITTED)) {
      submissionEnvelopeService.archiveSubmission(submissionEnvelope);
    } else if (currentState.equals(SubmissionState.ARCHIVING)) {
      // nothing to do, maybe log it
    } else if (currentState.equals(SubmissionState.ARCHIVED)) {
      // nothing to do, because we want to just archive
    } else {
      throw new RuntimeException(String.format("Unexpected action %s for JustArchive submission %s",
                                               currentState,
                                               submissionEnvelope.getId()));
    }
  }
}
