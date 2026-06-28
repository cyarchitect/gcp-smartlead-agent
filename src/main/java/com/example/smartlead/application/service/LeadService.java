package com.example.smartlead.application.service;

import com.example.smartlead.domain.model.GoldenLead;
import com.example.smartlead.domain.model.Lead;
import com.example.smartlead.domain.service.JaroWinklerMatcher;
import com.example.smartlead.infrastructure.adapter.GoldenLeadRepository;
import com.example.smartlead.infrastructure.adapter.LeadRepository;
import com.example.smartlead.infrastructure.config.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private GoldenLeadRepository goldenLeadRepository;

    @Autowired
    private JaroWinklerMatcher similarityMatcher;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * Seeds the Firestore collections with our initial LinkedIn-ready messy dataset.
     */
    public Mono<Void> seedDemoData() {
        List<Lead> demoLeads = Arrays.asList(
            Lead.builder()
                .id(UUID.randomUUID().toString())
                .name("John S.")
                .company("Google")
                .email("john.smith@google.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build(),
            Lead.builder()
                .id(UUID.randomUUID().toString())
                .name("J. Smith")
                .company("Alphabet Inc.")
                .email("jsmith@alphabet.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build(),
            Lead.builder()
                .id(UUID.randomUUID().toString())
                .name("Marc B.")
                .company("Salesforce")
                .email("marcb@salesforce.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build(),
            Lead.builder()
                .id(UUID.randomUUID().toString())
                .name("M. Benioff")
                .company("Salesforce Inc.")
                .email("benioff@salesforce.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build(),
            Lead.builder()
                .id(UUID.randomUUID().toString())
                .name("Alice J.")
                .company("Netflix")
                .email("alice@netflix.com")
                .status("RAW")
                .createdAt(System.currentTimeMillis())
                .build()
        );

        // Delete all existing leads and golden records, then insert the seed data
        return leadRepository.deleteAll()
                .then(goldenLeadRepository.deleteAll())
                .thenMany(leadRepository.saveAll(demoLeads))
                .then();
    }

    public Flux<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    public Flux<Lead> getRawLeads() {
        return leadRepository.findByStatus("RAW");
    }

    public Flux<GoldenLead> getAllGoldenLeads() {
        return goldenLeadRepository.findAll();
    }

    /**
     * Merges a list of raw Lead IDs into a new master GoldenRecord.
     */
    public Mono<GoldenLead> mergeLeads(List<String> rawLeadIds, String primaryLeadId) {
        if (rawLeadIds == null || rawLeadIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Raw lead IDs cannot be empty"));
        }

        // Fetch all the leads that are being merged
        return Flux.fromIterable(rawLeadIds)
                .flatMap(id -> leadRepository.findById(id))
                .collectList()
                .flatMap(leads -> {
                    // Find the primary lead which will act as the base for the GoldenLead
                    Lead primary = leads.stream()
                            .filter(l -> l.getId().equals(primaryLeadId))
                            .findFirst()
                            .orElse(leads.get(0));

                    // Create the GoldenLead entity
                    GoldenLead golden = GoldenLead.builder()
                            .id(UUID.randomUUID().toString())
                            .name(primary.getName())
                            .company(primary.getCompany())
                            .email(primary.getEmail())
                            .mergedRecordIds(rawLeadIds)
                            .updatedAt(System.currentTimeMillis())
                            .build();

                    // Save the GoldenLead, and update status of all merged raw leads
                    return goldenLeadRepository.save(golden)
                            .flatMap(savedGolden -> {
                                List<Mono<Lead>> updateOperations = new ArrayList<>();
                                for (Lead l : leads) {
                                    l.setStatus("MERGED");
                                    l.setMergedInto(savedGolden.getId());
                                    updateOperations.add(leadRepository.save(l));
                                }
                                return Mono.when(updateOperations)
                                        .then(Mono.fromRunnable(() -> eventPublisher.publish(savedGolden)))
                                        .thenReturn(savedGolden);
                            });
                });
    }

    /**
     * Enriches a lead record and publishes the update event.
     */
    public Mono<Lead> enrichLead(String leadId, String industry, int companySize, String techStack, String outreachMessage) {
        return leadRepository.findById(leadId)
                .flatMap(lead -> {
                    lead.setEnrichedIndustry(industry);
                    lead.setEnrichedSize(companySize);
                    lead.setEnrichedTechStack(techStack);
                    lead.setOutreachMessage(outreachMessage);
                    lead.setStatus("ENRICHED");
                    return leadRepository.save(lead)
                            .doOnNext(savedLead -> eventPublisher.publish(savedLead));
                });
    }

    /**
     * Uses the Jaro-Winkler engine to find potential duplicate leads.
     */
    public Flux<List<Lead>> findPotentialDuplicates(double threshold) {
        return leadRepository.findByStatus("RAW")
                .collectList()
                .flatMapMany(leads -> {
                    List<List<Lead>> duplicateGroups = new ArrayList<>();
                    boolean[] visited = new boolean[leads.size()];

                    for (int i = 0; i < leads.size(); i++) {
                        if (visited[i]) continue;
                        List<Lead> group = new ArrayList<>();
                        group.add(leads.get(i));

                        for (int j = i + 1; j < leads.size(); j++) {
                            if (visited[j]) continue;

                            Lead l1 = leads.get(i);
                            Lead l2 = leads.get(j);

                            // Calculate similarity on both name and company
                            double nameSim = similarityMatcher.calculateSimilarity(l1.getName(), l2.getName());
                            double companySim = similarityMatcher.calculateSimilarity(l1.getCompany(), l2.getCompany());

                            // If name matches strongly or company matches strongly with similar names, consider them duplicates
                            if (nameSim >= threshold && (companySim >= 0.6 || l1.getEmail().split("@")[0].equals(l2.getEmail().split("@")[0]))) {
                                group.add(l2);
                                visited[j] = true;
                            }
                        }

                        if (group.size() > 1) {
                            duplicateGroups.add(group);
                            visited[i] = true;
                        }
                    }
                    return Flux.fromIterable(duplicateGroups);
                });
    }
}
