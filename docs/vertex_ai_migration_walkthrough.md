# Walkthrough: Resolved Gemini API Configuration (404/429)

This walkthrough documents the technical updates made to migrate **SmartLead AI** to Vertex AI's enterprise Gemini endpoints, resolving the previous `RESOURCE_EXHAUSTED` (429) developer key limits and `NOT_FOUND` (404) model version errors.

## Changes Completed

### 1. Updated Model Configuration
* **File Modified:** [VertexAiConfig.java](file:///c:/Users/cyarc/.gemini/antigravity-ide/scratch/gcp-java-smartlead-agent/src/main/java/com/example/smartlead/infrastructure/config/VertexAiConfig.java)
* **Action:** 
  * Replaced the hardcoded deprecated model name `"gemini-1.5-flash"` with a dynamic property injection `@Value("${gemini.model.name:gemini-3.5-flash}")`.
  * Externalized the Google Cloud project ID and region location using `@Value` properties to make the config fully runtime-adjustable.

### 2. Resolved Dependency Mismatch (`NoClassDefFoundError`)
* **File Modified:** [pom.xml](file:///c:/Users/cyarc/.gemini/antigravity-ide/scratch/gcp-java-smartlead-agent/pom.xml)
* **Action:**
  * Added the Google Cloud Libraries BOM (`com.google.cloud:libraries-bom:26.46.0`) in the `dependencyManagement` block *before* the Spring Cloud GCP BOM. This aligns transitive gRPC and protobuf libraries to the correct versions needed by LangChain4j.
  * Declared the `com.google.cloud:google-cloud-vertexai` dependency explicitly.

### 3. Enabled Runtime Configuration
* **File Modified:** [application.properties](file:///c:/Users/cyarc/.gemini/antigravity-ide/scratch/gcp-java-smartlead-agent/src/main/resources/application.properties)
* **Action:** 
  * Defined default configurations for `gemini.location=us-central1` and `gemini.model.name=gemini-3.5-flash`.
  * Deployed with the environment variable `GEMINI_MODEL_NAME=gemini-2.5-flash` for high stability (Vertex AI Model Garden's current stable workhorse version).

---

## Verification Results

### 1. Database Seeding
We successfully hit the seeding endpoint to populate Firestore:
```powershell
Invoke-RestMethod -Uri "https://gcp-java-smartlead-agent-1085860688693.us-central1.run.app/api/leads/seed" -Method POST
# Response:
Database successfully seeded with messy leads.
```

### 2. AI Chat & Function Calling
We dispatched a test chat request to `/api/chat` using the deployed application. The backend successfully authenticated using the Cloud Run service account, called the Vertex AI endpoint, parsed our system prompt and custom tool metadata, and returned a correct, agent-aware response:

```json
{
  "message": "Who are you and what tools do you have?"
}
```
**Response from LLM:**
> Hello! I am SmartLead AI, a professional B2B Data Steward and Sales Operations Agent. My purpose is to help you enrich and deduplicate your sales leads.
> 
> I have access to the following tools:
> 1. **`findDuplicateCandidates(threshold: float)`**: This tool runs a similarity matching engine to identify groups of potential duplicate lead candidates...
> 2. **`mergeDuplicateLeads(rawLeadIds: list, primaryLeadId: str)`**: This tool merges duplicate raw leads...
> 3. **`getRawLeads()`**: This tool retrieves all raw, unprocessed sales leads...
> 4. **`enrichLeadRecord(leadId: str, ...)`**: This tool enriches a specific lead record...
