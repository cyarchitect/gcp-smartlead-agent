package com.example.smartlead.domain.model;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
