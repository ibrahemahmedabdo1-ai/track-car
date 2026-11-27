# Track Car

Track Car is a multi-device fleet tracking platform for Android head units and vehicle devices.

## Project structure
- `apps/admin-android`: Android fleet administration app.
- `apps/installer-android`: Android device diagnostics and installation preparation.
- `apps/agent-android`: vehicle-side Agent, enrollment, background GPS and API sync.
- `web/`: Next.js fleet console and authenticated device API.
- `backend/db/migrations/`: database migrations.
- `docs/`: architecture, compatibility and deployment notes.

## Build Android APKs
GitHub Actions builds debug APKs for Admin, Installer and Agent and uploads them as `track-car-android-apks` when the workflow succeeds.

## Backend
The Next.js application lives in `web/`. See [web/README.md](web/README.md) and [deployment setup](docs/DEPLOYMENT_SETUP.md).

## Compatibility and privacy
The Android Agent is designed for broad compatibility (API 21+) using native Android location APIs. Background tracking is enabled only after an authorized operator opts in and grants Android permissions. It remains visible through the Android location-service notification. OEM power-management restrictions require device-specific testing.

No universal root package is included; root methods are firmware- and model-specific.
