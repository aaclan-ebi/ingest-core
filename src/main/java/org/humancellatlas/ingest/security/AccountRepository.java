package org.humancellatlas.ingest.security;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RestResource(exported=false)
public interface AccountRepository extends MongoRepository<Account, String> {

    Account findByProviderReference(String providerReference);

    @Query(fields="{_id : 1, name: 1}")
    List<Account> findByRoles(String role);

}
