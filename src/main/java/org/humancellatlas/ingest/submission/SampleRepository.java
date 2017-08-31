package org.humancellatlas.ingest.submission;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
public interface SampleRepository extends MongoRepository<Sample, String> {
    public Sample findByUuid(UUID uuid);
}
