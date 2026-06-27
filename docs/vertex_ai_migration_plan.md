# Implementation Plan: Resolve Gemini API Issues (404/429)

This plan outlines how to resolve the Gemini API connection issue for **SmartLead AI** on Cloud Run.

## Problem Description
1. The **Google AI Studio API Key** (`GoogleAiGeminiChatModel`) fails with `RESOURCE_EXHAUSTED` (429) because the free-tier prepay credits for `generativelanguage.googleapis.com` are depleted (limit: 0).
2. When attempting to use **Vertex AI** (`VertexAiGeminiChatModel`), the request fails with `NOT_FOUND` (404) for `gemini-1.5-flash` because the `gemini-1.5-flash` model (specifically version `gemini-1.5-flash-001`) is no longer available/supported in Vertex AI's Model Garden for this project.

## Findings & Solution
* The project `project-3715c3f4-9240-400e-9f5` has billing enabled and has `aiplatform.googleapis.com` (Agent Platform API / Vertex AI API) enabled.
* The Cloud Run service account `1085860688693-compute@developer.gserviceaccount.com` has the `roles/aiplatform.user` role assigned.
* Checking the available models via `gcloud ai model-garden models list` shows that **`gemini-3.5-flash`** is available and supports predictions (`CAN_PREDICT = Yes`), whereas `gemini-1.5-flash` models are not available.
* By updating the model configuration in `VertexAiConfig.java` to use `"gemini-3.5-flash"`, we can leverage the enterprise Vertex AI API without requiring API key management or running into free-tier rate limits.

---

## Proposed Changes

### [Backend Configuration]

#### [MODIFY] [VertexAiConfig.java](file:///c:/Users/cyarc/.gemini/antigravity-ide/scratch/gcp-java-smartlead-agent/src/main/java/com/example/smartlead/infrastructure/config/VertexAiConfig.java)
Update the model name parameter from `"gemini-1.5-flash"` to `"gemini-3.5-flash"`.

---

## Deployment & Verification Plan

### Cloud Run Redeployment
We will build the container image using Cloud Build and deploy it to Cloud Run:
1. Build the Docker image:
   ```bash
   gcloud builds submit --tag us-central1-docker.pkg.dev/project-3715c3f4-9240-400e-9f5/cloud-run-source-deploy/gcp-java-smartlead-agent:latest --region us-central1 --project project-3715c3f4-9240-400e-9f5
   ```
2. Deploy the container to Cloud Run:
   ```bash
   gcloud run deploy gcp-java-smartlead-agent --image us-central1-docker.pkg.dev/project-3715c3f4-9240-400e-9f5/cloud-run-source-deploy/gcp-java-smartlead-agent:latest --region us-central1 --project project-3715c3f4-9240-400e-9f5 --allow-unauthenticated
   ```

### Manual Verification
1. Open the deployed application URL: `https://gcp-java-smartlead-agent-1085860688693.us-central1.run.app/`
2. Test lead seeding and AI enrichment.
3. Verify that the AI Chat sidebar functions correctly and responds to steward prompts without 404 or 429 errors.
