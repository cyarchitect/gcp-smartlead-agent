# SmartLead AI — Local Setup & Development Guide

This document describes how to set up, run, and develop the **SmartLead AI** application on a local development machine.

---

## 1. Prerequisites
- **Java 17 JDK** (e.g., Eclipse Temurin) installed and configured in your path (`JAVA_HOME`).
- **Node.js** (v18 or higher) & **npm** for frontend tooling.
- **Maven 3.x** (or use the packaged `./mvnw` wrapper).
- **Google Cloud CLI (`gcloud`)** for authenticating and configuring credentials.

---

## 2. Configuration & Authentication

### Gemini API Key (Required for AI features)
Get a Gemini API Key from [Google AI Studio](https://aistudio.google.com/).
Export it as an environment variable in your terminal session before launching the backend:

```powershell
# PowerShell
$env:GEMINI_API_KEY="AIzaSy..."

# Command Prompt (Windows)
set GEMINI_API_KEY=AIzaSy...

# Bash (Linux/macOS)
export GEMINI_API_KEY="AIzaSy..."
```

### GCP Authentication (Required for Firestore)
Log in with your GCP Credentials:
```bash
gcloud auth application-default login
gcloud config set project YOUR_GCP_PROJECT_ID
```

---

## 3. Running the Backend Locally
Execute the Spring Boot plugin goal:

```bash
# Using Maven wrapper
./mvnw spring-boot:run
```
The server will boot on port `8080`.

---

## 4. Running the Frontend Locally (Vite Dev Server)
To edit and see frontend changes instantly with hot-reloading:

1. Navigate to the React app workspace root.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
4. Access the UI at `http://localhost:5173`. API requests are proxied automatically to `http://localhost:8080` (configured in `vite.config.js`).

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
3. Configure Spring Boot to connect to the emulator by adding this line to [application.properties](file:///C:/Users/cyarc/.gemini/antigravity-ide/scratch/gcp-java-smartlead-agent/src/main/resources/application.properties):
   ```properties
   spring.cloud.gcp.firestore.host=localhost:8080
   ```
