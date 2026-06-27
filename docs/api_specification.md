# SmartLead REST API Specification

This document details the REST API endpoints provided by the **SmartLead AI** backend for managing leads, interacting with the AI Agent, and monitoring real-time processing progress.

---

## 1. Lead Management Endpoints

### Seed Demo Data
Populates the Firestore database with raw, messy mock leads to demonstrate deduplication and enrichment capabilities.

* **Endpoint:** `/api/leads/seed`
* **Method:** `POST`
* **Headers:** `Content-Type: application/json`
* **Response Status:** `200 OK`
* **Response Body:**
  ```text
  Database successfully seeded with messy leads.
  ```

---

### Get All Leads
Retrieves the complete list of lead records from Firestore.

* **Endpoint:** `/api/leads`
* **Method:** `GET`
* **Response Status:** `200 OK`
* **Response Body:**
  ```json
  [
    {
      "id": "1a669b07-beb2-4046-a2e6-f0bec7cca829",
      "name": "M. Benioff",
      "company": "Salesforce Inc.",
      "email": "benioff@salesforce.com",
      "status": "RAW",
      "enrichedIndustry": null,
      "enrichedSize": null,
      "enrichedTechStack": null,
      "outreachMessage": null,
      "mergedInto": null,
      "createdAt": 1782529324513
    },
    ...
  ]
  ```

---

### Reset Database
Clears all lead documents from the Firestore collection.

* **Endpoint:** `/api/leads/reset`
* **Method:** `POST`
* **Response Status:** `200 OK`
* **Response Body:**
  ```text
  Database successfully cleared.
  ```

---

## 2. AI Chat & Orchestration Endpoints

### Dispatch Chat Message
Sends a conversational command to the LangChain4j Agent.

* **Endpoint:** `/api/chat`
* **Method:** `POST`
* **Headers:** `Content-Type: application/json`
* **Request Body:**
  ```json
  {
    "message": "enrich all raw leads"
  }
  ```
* **Response Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "response": "I have successfully enriched all raw leads! John S. has been updated to Search/AI industry and Salesforce leads were updated."
  }
  ```

---

## 3. Real-Time Events (Server-Sent Events)

### Progress Update Stream
Provides a Server-Sent Events (SSE) channel for the client UI to listen to background processing changes in real-time.

* **Endpoint:** `/api/leads/progress`
* **Method:** `GET`
* **Headers:** `Accept: text/event-stream`
* **Event Payload Formats:**
  
  **1. Processing Stage Event:**
  ```text
  event: stage
  data: {"stage": "enriching", "message": "Enriching lead: John S. (Google)"}
  ```

  **2. Complete Event:**
  ```text
  event: complete
  data: {"message": "All enrichment operations completed successfully."}
  ```

  **3. Lead State Changed Event:**
  ```text
  event: lead-updated
  data: {
    "id": "4056419a-2dfc-4f4a-bf8b-4eed9fca8cbf",
    "name": "John S.",
    "company": "Google",
    "status": "ENRICHED",
    "enrichedIndustry": "Search/AI",
    "enrichedSize": "100,000+",
    "enrichedTechStack": "Java, GCP, Angular",
    "outreachMessage": "Hi John, saw you are leading tech at Google..."
  }
  ```
