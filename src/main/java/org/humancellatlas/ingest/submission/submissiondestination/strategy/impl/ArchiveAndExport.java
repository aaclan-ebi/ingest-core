package org.humancellatlas.ingest.submission.submissiondestination.strategy.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeService;
import org.humancellatlas.ingest.submission.submissiondestination.strategy.SubmissionStrategy;

@RequiredArgsConstructor
public class ArchiveAndExport implements SubmissionStrategy {
  private final @NonNull SubmissionEnvelopeService submissionEnvelopeService;

  @Override
  public void handleState(SubmissionEnvelope envelope, SubmissionState currentState) {
    if(currentState.equals(SubmissionState.SUBMITTED)) {
      submissionEnvelopeService.archiveSubmission(envelope);
    } else if (currentState.equals(SubmissionState.ARCHIVING)) {
      // nothing to do
      return;
    } else if (currentState.equals(SubmissionState.ARCHIVED)) {
      submissionEnvelopeService.exportSubmission(envelope);
    } else if (currentState.equals(SubmissionState.EXPORTING)) {
      // nothing to do
      return;
    } else if (currentState.equals(SubmissionState.EXPORTED)) {
      submissionEnvelopeService.handleCommitExported(envelope);
    } else if (currentState.equals(SubmissionState.CLEANUP)) {
      // nothing to do
    } else if (currentState.equals(SubmissionState.COMPLETE)) {
      // nothing to do
    } else {
      throw new RuntimeException(String.format("Unexpected action %s for ArchiveAndExport submission %s",
                                               currentState,
                                               envelope.getId()));
    }
  }
}
