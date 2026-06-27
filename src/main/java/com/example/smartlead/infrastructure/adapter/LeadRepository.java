package com.example.smartlead.infrastructure.adapter;

import com.example.smartlead.domain.model.Lead;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface LeadRepository extends FirestoreReactiveRepository<Lead> {
    Flux<Lead> findByStatus(String status);
    Flux<Lead> findByMergedInto(String mergedIntoId);
}
