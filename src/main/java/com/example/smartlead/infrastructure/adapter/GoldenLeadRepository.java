package com.example.smartlead.infrastructure.adapter;

import com.example.smartlead.domain.model.GoldenLead;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoldenLeadRepository extends FirestoreReactiveRepository<GoldenLead> {
}
