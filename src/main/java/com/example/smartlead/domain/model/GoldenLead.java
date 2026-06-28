package com.example.smartlead.domain.model;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;

import java.util.List;

@Document(collectionName = "golden_leads")
public class GoldenLead {
    @DocumentId
    private String id;
    
    private String name;
    private String company;
    private String email;
    
    // MDM tracing
    private List<String> mergedRecordIds; // List of raw Lead IDs that were consolidated into this record
    private Long updatedAt;

    public GoldenLead() {}

    public GoldenLead(String id, String name, String company, String email, List<String> mergedRecordIds, Long updatedAt) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.email = email;
        this.mergedRecordIds = mergedRecordIds;
        this.updatedAt = updatedAt;
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

    public List<String> getMergedRecordIds() { return mergedRecordIds; }
    public void setMergedRecordIds(List<String> mergedRecordIds) { this.mergedRecordIds = mergedRecordIds; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    // Builder pattern
    public static GoldenLeadBuilder builder() {
        return new GoldenLeadBuilder();
    }

    public static class GoldenLeadBuilder {
        private String id;
        private String name;
        private String company;
        private String email;
        private List<String> mergedRecordIds;
        private Long updatedAt;

        public GoldenLeadBuilder id(String id) { this.id = id; return this; }
        public GoldenLeadBuilder name(String name) { this.name = name; return this; }
        public GoldenLeadBuilder company(String company) { this.company = company; return this; }
        public GoldenLeadBuilder email(String email) { this.email = email; return this; }
        public GoldenLeadBuilder mergedRecordIds(List<String> mergedRecordIds) { this.mergedRecordIds = mergedRecordIds; return this; }
        public GoldenLeadBuilder updatedAt(Long updatedAt) { this.updatedAt = updatedAt; return this; }

        public GoldenLead build() {
            return new GoldenLead(id, name, company, email, mergedRecordIds, updatedAt);
        }
    }
}
