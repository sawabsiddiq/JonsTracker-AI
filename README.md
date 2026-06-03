# JobTrack AI - Advanced Local Job Application Tracker

JobTrack AI is a highly secure, client-first Android application designed to track and organize your job search. It utilizes the power of **Google Gemini AI** to extract application details, interview updates, and rejection alerts securely via direct REST API communication, keeping all your personal data stored locally on-device.

---

## 🚀 Key Features & Architecture

- **Local-First Persistence**: Your private application tracking logs, credentials, and message snippets are persisted offline in a secure, local Room SQLite database on your device.
- **AI Inbox**: All AI actions go through a staging **AI Inbox** review flow. Extracted applications are *never* automatically injected into your active pipeline without explicit verification—you have final edit/confirm/ignore control.
- **Gmail Scan & Extraction**: Scan of recent messages can retrieve high-level metadata (up to 15 days back) to inspect for relevant updates, sending only relevant, trimmed text excerpts to Google's Gemini models for structural conversion.
- **Secure Storage**: Sensitive preferences and keys fail closed. If hardware-backed secure encryption storage (`EncryptedSharedPreferences`) is unavailable, Gmail scanning is securely disabled.
- **No Telemetry & Tracking**: Zero background tracking or analytical telemetries are included. Under no circumstances are raw email body payloads uploaded, cached, or trained on.

---

## ⚙️ Project Configuration & Installation

### 1. Prerequisites
- **Android SDK**: Compile Sdk Level `36` / Target Sdk Level `36` / Min Sdk Level `24`
- **Gradle**: Kotlin DSL implementation (`build.gradle.kts`)
- **API Access**: A valid Google Gemini API Key

### 2. Environment Setup
Create a `.env` file at the root of your project directory using `.env.example` as a template:

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

*Note: In production Android builds, these keys are securely made available in code via `BuildConfig.GEMINI_API_KEY` through the Secrets Gradle Plugin.*

### 3. V1 Gmail OAuth Disclaimer
Gmail connection is currently designed to require a production Google Cloud Platform (GCP) OAuth client setup. Unless the app is running in a fully synchronized Google sign-in configuration:
- In-app scanning utilizes local parser scenarios or prompts indicating GCP OAuth verification is active.
- If real OAuth credentials are not authorized, the interface shows: **`Gmail sync requires production OAuth setup.`**

---

## 🔒 Privacy & Data Protection Model

Your data privacy is our absolute priority. The extraction system enforces the following rigid rules:
1. **Plain, Accurate Disclosure**: To parse and auto-classify relevant applications, selected email body excerpts are sent directly to the official Google Gemini API via HTTPS. 
2. **On-Device Database Storage**: Your primary job tracker databases are offline, stored as a standard SQLite architecture in your local app sandbox.
3. **No Secondary Clouds**: We run no private backends or data sync systems.
4. **Complete User Erasure**: You can wipe all applications, events, and Gmail history anytime through the **Settings Screen**.

---

## 🛠️ Build & Run Commands

You can execute basic gradle commands using standard terminal interfaces:

- **Build Project**:
  ```bash
  gradle assembleDebug
  ```

- **Run Standard Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

---

## 📋 Pre-Release Quality Checklist

Prior to compiling production release bundles (AABs/APKs):
- Ensure `android:allowBackup` is explicitly structured securely in `AndroidManifest.xml`.
- Confirm that no test credentials or manual OAuth access tokens are stored in the final production code base.
- Verify that standard Gradle namespace configurations (`com.jobtrackai.app`) match modern target profiles.
