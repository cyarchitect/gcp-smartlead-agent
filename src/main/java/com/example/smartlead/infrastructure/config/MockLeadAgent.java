package com.example.smartlead.infrastructure.config;

import com.example.smartlead.application.service.LeadAgent;
import com.example.smartlead.application.service.LeadService;
import com.example.smartlead.domain.model.Lead;
import com.example.smartlead.domain.model.GoldenLead;
import java.util.List;
import java.util.stream.Collectors;

public class MockLeadAgent implements LeadAgent {

    private final LeadService leadService;

    public MockLeadAgent(LeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public String chat(String userMessage) {
        if (userMessage == null) {
            return "Please provide a message.";
        }

        String msg = userMessage.toLowerCase();

        if (msg.contains("enrich")) {
            List<Lead> rawLeads = leadService.getRawLeads().collectList().block();
            if (rawLeads == null || rawLeads.isEmpty()) {
                return "SmartLead AI (Mock Mode): I scanned the database and found 0 raw, unprocessed leads. Please seed the demo data first.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SmartLead AI (Mock Mode): Successfully enriched ").append(rawLeads.size()).append(" raw leads:\n\n");

            for (Lead lead : rawLeads) {
                String industry;
                int size;
                String techStack;
                String outreach;

                String comp = lead.getCompany().toLowerCase();
                if (comp.contains("google") || comp.contains("alphabet")) {
                    industry = "Search & Cloud SaaS";
                    size = 150000;
                    techStack = "Java, GCP, Angular, Kubernetes";
                    outreach = "Hi " + lead.getName() + ",\n\nI noticed your interesting work at " + lead.getCompany() + ". Let's explore how our AI data cleansing pipeline can optimize your sales operations on Google Cloud Platform.\n\nBest,\nSales Ops Agent";
                } else if (comp.contains("salesforce")) {
                    industry = "CRM & Cloud SaaS";
                    size = 75000;
                    techStack = "Apex, AWS, React, Slack";
                    outreach = "Hi " + lead.getName() + ",\n\nHope all is well. As " + lead.getCompany() + " drives CRM innovation, our SmartLead integration could be a game-changer for your lead routing hygiene.\n\nBest,\nSales Ops Agent";
                } else if (comp.contains("netflix")) {
                    industry = "Entertainment & Streaming";
                    size = 12000;
                    techStack = "Java, AWS, React, NodeJS";
                    outreach = "Hi " + lead.getName() + ",\n\nI love streaming on Netflix! Our B2B platform can help you clean up and enrich partner databases to target entertainment affiliates.\n\nBest,\nSales Ops Agent";
                } else {
                    industry = "Technology";
                    size = 5000;
                    techStack = "Java, Spring Boot, React, PostgreSQL";
                    outreach = "Hi " + lead.getName() + ",\n\nI wanted to reach out regarding your tech stack at " + lead.getCompany() + ". Our AI agent solves messy lead duplicates seamlessly.\n\nBest,\nSales Ops Agent";
                }

                // Call the service to enrich the database and trigger the SSE event
                leadService.enrichLead(lead.getId(), industry, size, techStack, outreach).block();

                sb.append("- **").append(lead.getName()).append("** (").append(lead.getCompany()).append(") enriched -> Industry: ")
                  .append(industry).append(", Size: ").append(String.format("%,d", size)).append(", Tech Stack: ").append(techStack).append("\n");
            }
            return sb.toString();
        } else if (msg.contains("merge")) {
            List<List<Lead>> duplicateGroups = leadService.findPotentialDuplicates(0.7).collectList().block();
            if (duplicateGroups == null || duplicateGroups.isEmpty()) {
                return "SmartLead AI (Mock Mode): I did not find any duplicate groups to merge.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SmartLead AI (Mock Mode): Successfully resolved duplicates into Golden Master Records:\n\n");

            for (List<Lead> group : duplicateGroups) {
                if (group.size() < 2) continue;

                // Pick primary: one with the longest name or first in group
                Lead primary = group.get(0);
                for (Lead l : group) {
                    if (l.getName().length() > primary.getName().length()) {
                        primary = l;
                    }
                }

                List<String> rawLeadIds = group.stream().map(Lead::getId).collect(Collectors.toList());

                // Merge via LeadService (this also saves to GoldenLeadRepository and updates raw leads, publishing SSE event)
                GoldenLead golden = leadService.mergeLeads(rawLeadIds, primary.getId()).block();

                if (golden != null) {
                    sb.append("✨ **Golden Record Created**: ").append(golden.getName()).append(" @ ").append(golden.getCompany())
                      .append(" (Email: ").append(golden.getEmail()).append(")\n")
                      .append("  - Merged Raw IDs: ").append(String.join(", ", rawLeadIds)).append("\n\n");
                }
            }
            return sb.toString();
        } else if (msg.contains("duplicate") || msg.contains("find")) {
            List<List<Lead>> duplicateGroups = leadService.findPotentialDuplicates(0.7).collectList().block();
            if (duplicateGroups == null || duplicateGroups.isEmpty()) {
                return "SmartLead AI (Mock Mode): I scanned the database and found 0 potential duplicate lead groups.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SmartLead AI (Mock Mode): Identified ").append(duplicateGroups.size()).append(" duplicate groups:\n\n");
            for (int i = 0; i < duplicateGroups.size(); i++) {
                List<Lead> group = duplicateGroups.get(i);
                sb.append("Group ").append(i + 1).append(":\n");
                for (Lead l : group) {
                    sb.append("  - ").append(l.getName()).append(" @ ").append(l.getCompany()).append(" (").append(l.getEmail()).append(")\n");
                }
                sb.append("\n");
            }
            sb.append("You can tell me to 'merge duplicates' to consolidate these records.");
            return sb.toString();
        }

        return "SmartLead AI (Mock Mode): Hello! I am in mock development mode. I support the following actions:\n" +
               "1. **'enrich'**: Deterministically enriches messy raw leads.\n" +
               "2. **'find duplicates'**: Searches for duplicate groups using Jaro-Winkler.\n" +
               "3. **'merge duplicates'**: Consolidates matches into Golden Master Records.";
    }
}
