package com.example.smartlead.domain.model;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;

@Document(collectionName = "leads")
public class Lead {
    @DocumentId
    private String id;
    
    private String name;
    private String company;
    private String email;
    
    // Status can be: RAW, ENRICHED, MERGED
    private String status;
    
    // Enriched fields from AI Agent
    private String enrichedIndustry;
    private Integer enrichedSize;
    private String enrichedTechStack;
    private String outreachMessage;
    
    // MDM tracing fields
    private String mergedInto; // ID of the GoldenLead if this lead was merged
    private Long createdAt;

    public Lead() {}

    public Lead(String id, String name, String company, String email, String status, 
                String enrichedIndustry, Integer enrichedSize, String enrichedTechStack, 
                String outreachMessage, String mergedInto, Long createdAt) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.email = email;
        this.status = status;
        this.enrichedIndustry = enrichedIndustry;
        this.enrichedSize = enrichedSize;
        this.enrichedTechStack = enrichedTechStack;
        this.outreachMessage = outreachMessage;
        this.mergedInto = mergedInto;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEnrichedIndustry() { return enrichedIndustry; }
    public void setEnrichedIndustry(String enrichedIndustry) { this.enrichedIndustry = enrichedIndustry; }

    public Integer getEnrichedSize() { return enrichedSize; }
    public void setEnrichedSize(Integer enrichedSize) { this.enrichedSize = enrichedSize; }

    public String getEnrichedTechStack() { return enrichedTechStack; }
    public void setEnrichedTechStack(String enrichedTechStack) { this.enrichedTechStack = enrichedTechStack; }

    public String getOutreachMessage() { return outreachMessage; }
    public void setOutreachMessage(String outreachMessage) { this.outreachMessage = outreachMessage; }

    public String getMergedInto() { return mergedInto; }
    public void setMergedInto(String mergedInto) { this.mergedInto = mergedInto; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    // Builder pattern
    public static LeadBuilder builder() {
        return new LeadBuilder();
    }

    public static class LeadBuilder {
        private String id;
        private String name;
        private String company;
        private String email;
        private String status;
        private String enrichedIndustry;
        private Integer enrichedSize;
        private String enrichedTechStack;
        private String outreachMessage;
        private String mergedInto;
        private Long createdAt;

        public LeadBuilder id(String id) { this.id = id; return this; }
        public LeadBuilder name(String name) { this.name = name; return this; }
        public LeadBuilder company(String company) { this.company = company; return this; }
        public LeadBuilder email(String email) { this.email = email; return this; }
        public LeadBuilder status(String status) { this.status = status; return this; }
        public LeadBuilder enrichedIndustry(String enrichedIndustry) { this.enrichedIndustry = enrichedIndustry; return this; }
        public LeadBuilder enrichedSize(Integer enrichedSize) { this.enrichedSize = enrichedSize; return this; }
        public LeadBuilder enrichedTechStack(String enrichedTechStack) { this.enrichedTechStack = enrichedTechStack; return this; }
        public LeadBuilder outreachMessage(String outreachMessage) { this.outreachMessage = outreachMessage; return this; }
        public LeadBuilder mergedInto(String mergedInto) { this.mergedInto = mergedInto; return this; }
        public LeadBuilder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }

        public Lead build() {
            return new Lead(id, name, company, email, status, enrichedIndustry, enrichedSize, enrichedTechStack, outreachMessage, mergedInto, createdAt);
        }
    }
}
