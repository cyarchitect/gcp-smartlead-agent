package com.example.smartlead.domain.model;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
