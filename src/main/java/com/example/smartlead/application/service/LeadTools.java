package com.example.smartlead.application.service;

import com.example.smartlead.domain.model.GoldenLead;
import com.example.smartlead.domain.model.Lead;
import com.example.smartlead.infrastructure.adapter.LeadRepository;
import com.example.smartlead.infrastructure.config.EventPublisher;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeadTools {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Tool("Retrieves all raw, unprocessed sales leads from the database")
    public List<Lead> getRawLeads() {
        return leadService.getAllLeads()
                .filter(l -> "RAW".equals(l.getStatus()))
                .collectList()
                .block();
    }

    @Tool("Runs the similarity matching engine to identify groups of duplicate lead candidates. Needs a threshold (e.g. 0.7)")
    public List<List<Lead>> findDuplicateCandidates(double threshold) {
        return leadService.findPotentialDuplicates(threshold)
                .collectList()
                .block();
    }

    @Tool("Enriches a specific lead record with company profile info (industry, company size, tech stack) and a personalized outreach message. Mark status as ENRICHED.")
    public Lead enrichLeadRecord(String leadId, String industry, int companySize, String techStack, String outreachMessage) {
        Lead lead = leadRepository.findById(leadId).block();
        if (lead != null) {
            lead.setEnrichedIndustry(industry);
            lead.setEnrichedSize(companySize);
            lead.setEnrichedTechStack(techStack);
            lead.setOutreachMessage(outreachMessage);
            lead.setStatus("ENRICHED");
            
            Lead savedLead = leadRepository.save(lead).block();
            
            // Publish event for real-time SSE push
            if (savedLead != null) {
                eventPublisher.publish(savedLead);
            }
            return savedLead;
        }
        return null;
    }

    @Tool("Merges a list of duplicate raw lead IDs into a single consolidated Golden Record. The primaryLeadId is the lead whose basic contact info should be preserved.")
    public GoldenLead mergeDuplicateLeads(List<String> rawLeadIds, String primaryLeadId) {
        GoldenLead golden = leadService.mergeLeads(rawLeadIds, primaryLeadId).block();
        if (golden != null) {
            // Publish event for real-time SSE push
            eventPublisher.publish(golden);
        }
        return golden;
    }
}
