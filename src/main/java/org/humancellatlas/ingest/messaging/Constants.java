package org.humancellatlas.ingest.messaging;

/**
 * @author Simon Jupp
 * @date 04/09/2017
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class Constants {
    public class Queues {
        public static final String FILE_UPDATE = "ingest.file.update.queue";
        public static final String SUBMISSION_UPDATE = "ingest.submission.update.queue";
    }

    public class Exchanges {
        public static final String FILE_FANOUT = "ingest.file.update.exchange";
        public static final String SUBMISSION_FANOUT = "ingest.submission.update.exchange";
    }
}