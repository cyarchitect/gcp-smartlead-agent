# SmartLead AI — Local Setup & Development Guide

This document describes how to set up, run, and develop the **SmartLead AI** application on a local development machine.

---

## 1. Prerequisites
- **Java 17 JDK** (or higher) installed and configured in your path (`JAVA_HOME`).
- **Node.js** (v18 or higher) & **npm** for frontend tooling.
- **Maven 3.x** (or use local mvn commands).
- **Google Cloud CLI (`gcloud`)** for authenticating and configuring credentials (only needed for real GCP / Vertex AI runs).

---

## 2. Pluggable AI Configuration

The application is pre-configured to run out-of-the-box in **Mock Mode**, meaning it requires zero external setups, credentials, or billing connections.

### Mode A: Mock Mode (Default)
No setup required! Simply run the application. The system will mock the LLM agent while executing the same underlying Reactive workflows, saving to database/emulators, and dispatching Server-Sent Events (SSE) updates to the UI Kanban board.

### Mode B: Google AI Developer API Mode
1. Get a Gemini API Key from [Google AI Studio](https://aistudio.google.com/).
2. Export it as an environment variable in your terminal session before launching the backend:
   ```powershell
   # PowerShell
   $env:GEMINI_API_KEY="AIzaSy..."

   # Command Prompt
   set GEMINI_API_KEY=AIzaSy...

   # Bash
   export GEMINI_API_KEY="AIzaSy..."
   ```
3. Set the property in [application.properties](file:///src/main/resources/application.properties):
   ```properties
   ai.provider=google-ai
   gemini.api.key=${GEMINI_API_KEY}
   ```

### Mode C: Vertex AI Mode
1. Authenticate with Google Cloud:
   ```bash
   gcloud auth application-default login
   gcloud config set project YOUR_GCP_PROJECT_ID
   ```
2. Set the properties in [application.properties](file:///src/main/resources/application.properties):
   ```properties
   ai.provider=vertex-ai
   spring.cloud.gcp.project-id=YOUR_GCP_PROJECT_ID
   ```

---

## 3. Running the Backend Locally
Execute the Spring Boot plugin goal:

```bash
mvn spring-boot:run
```
The server will boot on port `8080`.

---

## 4. Running the Frontend Locally (Vite Dev Server)
To edit and see frontend changes instantly with hot-reloading:

1. Navigate to the React app workspace root:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
4. Access the UI at [http://localhost:5173](http://localhost:5173). API requests are proxied automatically to [http://localhost:8080](http://localhost:8080).

---

## 5. Local Firestore Emulator Setup (Optional)
To avoid running queries against the live cloud database, you can run the local Firestore emulator:

1. Install the Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```
2. Start the emulator:
   ```bash
   firebase emulators:start --only firestore
   ```
3. Configure Spring Boot to connect to the emulator by adding this line to [application.properties](file:///src/main/resources/application.properties):
   ```properties
   spring.cloud.gcp.firestore.host=localhost:8080
   ```
