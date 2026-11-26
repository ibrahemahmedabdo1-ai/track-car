# Track Car

Fleet tracking platform monorepo. The first deliverable is a native Android Admin application; backend services and the vehicle Agent application will be added as separate modules.

## Modules
- `apps/admin-android`: native Kotlin + Jetpack Compose admin app.
- `backend/`: reserved for the authenticated fleet API.
- `apps/agent-android/`: reserved for the vehicle tracking agent.

## Build the Admin APK
The GitHub Actions workflow **Android Admin APK** builds a debug APK on pushes and manual runs. Download the `track-car-admin-debug-apk` artifact from the workflow run.

Local build (requires JDK 17, Android SDK 35, and Gradle 8.9):
```bash
gradle assembleDebug
```

## Current implementation status
The initial commit provides the Android app shell and CI build pipeline. Live vehicle data, authentication, maps, remote support, and backend integration are not enabled yet; they must be implemented and configured before production use.

## Security baseline
- Never commit API keys, signing keys, database credentials, or production tokens.
- Use HTTPS and short-lived authenticated sessions for future API calls.
- Remote device actions must be permission-gated, auditable, and visible to the device user.
