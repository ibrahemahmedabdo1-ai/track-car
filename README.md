# Track Car

Multi-device Android fleet platform.

## Modules
- `apps/admin-android`: Android owner/admin application.
- `apps/installer-android`: Android device diagnostics and setup foundation.
- `apps/agent-android`: vehicle Agent foundation.
- `docs/`: architecture and device compatibility policy.

## Android builds
The GitHub Actions workflow **Android Admin APK** builds all Android application modules. Download the APK artifacts from the workflow run.

## Vercel domains
- Main platform: https://track-car-phi.vercel.app
- Installer entry: https://track-car-installer.vercel.app

These are Vercel-provided project domains. The web/API and database integration are still under development.

## Current status
The Admin app has an initial dashboard shell. Installer and Agent modules now provide device/profile foundations only. They do not yet enroll devices, collect location, install another APK silently, or modify firmware/root state.

## Security
- No universal root image exists for all Android devices.
- Never commit API keys, signing keys, database credentials, or production tokens.
- Use HTTPS, signed packages, short-lived enrollment credentials and audit logs.
- Tracking must be disclosed and enabled with the appropriate Android permissions.
