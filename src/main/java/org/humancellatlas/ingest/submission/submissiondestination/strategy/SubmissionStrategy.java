package org.humancellatlas.ingest.submission.submissiondestination.strategy;

import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

public interface SubmissionStrategy {
  void handleState(SubmissionEnvelope envelope, SubmissionState currentState);
}
