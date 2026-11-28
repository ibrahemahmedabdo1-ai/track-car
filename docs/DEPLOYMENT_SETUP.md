# Track Car deployment and release setup

Last verified: 2026-11-28

## Live production website and API

- Vercel project: `track-car-platform`
- Root directory: `web`
- Production URL: https://track-car-platform-git-main-otpprovider.vercel.app
- Neon project: `silent-shape-33906165`
- Production branch: `production` (`br-lively-sea-b5o9hotj`)
- Database: `trackcar`
- Runtime database role: `trackcar_runtime`

The deployment completed successfully. Live `GET /api/health` previously returned HTTP 200 with `ok: true` and `database: connected`.

## Vercel environment variables

Keep all values server-side. Do not prefix secrets or database URLs with `NEXT_PUBLIC_`.

- `TRACKCAR_DATABASE_URL`: pooled or direct Neon connection string for the production branch and runtime role `trackcar_runtime`. The application prefers this variable over `DATABASE_URL`.
- `DATABASE_URL`: retained as a fallback for existing environments.
- `TRACKCAR_ADMIN_KEY`: strong random administrator key. Never include it in the APK or source control.
- `NEXT_PUBLIC_APP_URL`: optional. If omitted, install links use the stable production alias above.

## Installation links — current approved behavior

- Admin creates a reusable installation link with an optional fleet/company label.
- There is **no automatic expiry**. The database stores `expires_at = NULL`.
- There is **no enrollment-use limit**. The database stores `max_uses = NULL`.
- The same link can enroll multiple Android devices. Each enrollment creates a separate device record, unique device code, unique device token, separate consent record, and device events.
- Every device must independently accept the location-consent step and grant the Android permissions required by the Agent.
- Admin can revoke the shared link at any time. Revocation prevents future enrollments through that link; already-enrolled devices remain separate records and are not automatically deleted or disabled.
- The invitation token is stored as a SHA-256 hash in Postgres; the raw token is shown only in the generated URL.

The production schema was migrated on 2026-11-28 to allow nullable `expires_at` and `max_uses`. Existing finite/one-use invitations retain their original values and behavior.

## Android test build

The unified Android application is built by GitHub Actions. The current public test APK remains DEBUG-signed and is for internal testing only:

https://github.com/ibrahemahmedabdo1-ai/track-car/releases/download/track-car-android-latest/TrackCar.apk

## Signed production APK

A manual release workflow is prepared at `.github/workflows/android-release.yml`. This is the first official release, so a new company-controlled signing keystore must be created once and preserved securely for all future updates.

Before running it, add these four GitHub Actions repository secrets:

1. `TRACKCAR_RELEASE_KEYSTORE_BASE64` — Base64-encoded Android release keystore.
2. `TRACKCAR_RELEASE_KEYSTORE_PASSWORD` — keystore password.
3. `TRACKCAR_RELEASE_KEY_ALIAS` — signing key alias.
4. `TRACKCAR_RELEASE_KEY_PASSWORD` — key password.

Never commit the keystore or passwords to the repository or send them in chat. Losing the keystore can prevent upgrades of the installed app.

After adding all four secrets, run **Track Car Signed Production APK** from GitHub Actions using workflow_dispatch. It uploads `TrackCar-release.apk` as an artifact. The release workflow does not publish a public release automatically.

## Vehicle installation flow

1. Admin creates a reusable installation link.
2. The recipient downloads the APK and confirms installation in Android.
3. The recipient opens the invitation link again and selects Open Track Car.
4. The app passes the same reusable invite token to Vehicle Agent.
5. The user explicitly grants location consent and Android permissions on that device.
6. Backend creates an independent device record and returns a unique device token.
7. Repeat steps 2–6 for every additional device using the same link. Each device remains separately visible and manageable in Admin.

Android installation always requires the device user to confirm; this is not a silent installer.

## Remaining real-device checks

- Create one link and enroll at least two physical Android devices from it; verify two independent device records.
- Revoke the link and verify new enrollments are rejected while existing devices remain registered.
- Confirm foreground GPS updates, battery heartbeat, offline queue recovery, and stop control.
- Test background/reboot behavior on each target head-unit model. Android/OEM policies vary.
- Create the first release keystore and add the four GitHub Actions secrets before distributing production updates.

Existing custom domains and older Vercel projects have not been migrated.
