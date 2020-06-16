package org.humancellatlas.ingest.submission.submissiondestination;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.submissiondestination.strategy.SubmissionStrategy;
import org.humancellatlas.ingest.submission.submissiondestination.strategy.impl.ArchiveAndExport;
import org.humancellatlas.ingest.submission.submissiondestination.strategy.impl.JustArchive;
import org.humancellatlas.ingest.submission.submissiondestination.strategy.impl.JustExport;

@RequiredArgsConstructor
public class Utils {
  private final @NonNull JustArchive justArchive;
  private final @NonNull JustExport justExport;
  private final @NonNull ArchiveAndExport archiveAndExport;

  public SubmissionStrategy strategyForDestination(SubmissionDestination destination) {
    if(destination.equals(SubmissionDestination.ARCHIVE)) {
      return justArchive;
    } else if (destination.equals(SubmissionDestination.EXPORT)) {
      return justExport;
    } else if (destination.equals(SubmissionDestination.ARCHIVE_AND_EXPORT)) {
      return archiveAndExport;
    } else {
      throw new RuntimeException(String.format("Unknown submission destination: %s", destination));
    }
  }
}
