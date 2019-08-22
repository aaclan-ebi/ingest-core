package org.humancellatlas.ingest.stagingjob;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StagingJobService {
    private final @NonNull StagingJobRepository stagingJobRepository;

    public StagingJob registerNewJob(UUID stagingAreaUuid, String stagingAreaFileName) {
        try{
            StagingJob stagingJob = new StagingJob(stagingAreaUuid, stagingAreaFileName);
            return stagingJobRepository.save(stagingJob);
        } catch (DuplicateKeyException e) {
            throw new JobAlreadyRegisteredException(String.format("Staging job request already exists for file %s at upload area %s",
                                                                  stagingAreaFileName, stagingAreaUuid));
        }
    }

    public StagingJob completeJob(StagingJob stagingJob, String stagingAreaUri) {
        stagingJob.setStagingAreaFileUri(stagingAreaUri);
        return stagingJobRepository.save(stagingJob);
    }

    public void deleteJobsForStagingArea(UUID stagingAreaUuid) {
        stagingJobRepository.deleteAllByStagingAreaUuid(stagingAreaUuid);
    }

    private static class JobAlreadyRegisteredException extends IllegalStateException {
        JobAlreadyRegisteredException(String message){
            super(message);
        }
    }
}
