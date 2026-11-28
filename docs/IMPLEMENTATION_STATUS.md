# Track Car — Current Implementation Status

Last verified: 2026-11-28

## Current product structure

- Repository: https://github.com/ibrahemahmedabdo1-ai/track-car
- Unified Android application module: `apps/trackcar-android`
- Android workspaces: Admin, Vehicle Agent, and device diagnostics.
- Web platform and API: `web/`
- Production Vercel project: `track-car-platform` (root directory `web`)
- Neon production database: project `silent-shape-33906165`, branch `production` (`br-lively-sea-b5o9hotj`), database `trackcar`.

## Approved installation-link behavior

This is the current policy and supersedes all earlier 24-hour or one-time-link wording:

- Admin creates one reusable installation link.
- The link has no automatic expiry: `expires_at = NULL`.
- The link has no maximum enrollment count: `max_uses = NULL`.
- The same link can enroll multiple Android devices.
- Each enrollment creates a separate device row, unique device code, unique device API token, consent row, and enrollment event.
- The device name supplied by each enrolling device is saved independently; devices are not merged.
- Every device user must separately confirm the in-app location notice and grant Android permissions.
- Admin may revoke the link at any time. Revocation blocks future enrollments through that link but does not remove or revoke already-enrolled devices.
- Raw invitation tokens are generated randomly and only their SHA-256 hashes are stored in Neon.

### Database and live API verification

- Production migration applied on 2026-11-28: `pairing_invites.expires_at` and `pairing_invites.max_uses` are nullable.
- Live validation test on 2026-11-28 inserted a temporary unlimited invitation, called the deployed `GET /api/install/[token]`, and received `valid: true`, `expires_at: null`, `multi_device: true`, and `unlimited: true`.
- The temporary test invitation was deleted immediately after the check.
- This verifies the deployed link-validation policy. A complete two-physical-device enrollment test is still outstanding.

## Web platform and deployment

- Production URL: https://track-car-platform-git-main-otpprovider.vercel.app
- Latest security-header deployment: `dpl_DeByAa8GuG6RNDBekyHd5JuZEeCx`
- Deployment state: READY
- Commit: `e47ce7a71f75269884cb6500ed6ab86838ed01a9`
- GitHub Web Build run `46100686520`: success.
- Live `GET /api/health`: HTTP 200, `ok: true`, `database: connected`.
- Verified live headers include `Referrer-Policy: no-referrer`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, HSTS, and `Permissions-Policy`.
- Installation pages return no-store and noindex headers; install-token validation responses return no-store.
- The production runtime uses the server-side Neon role `trackcar_runtime` via `TRACKCAR_DATABASE_URL`; `TRACKCAR_ADMIN_KEY` remains server-side.

## Android application and latest test APK

- One unified Android APK includes Admin, Vehicle Agent, and device diagnostics workspaces.
- Foreground GPS service displays a persistent Android notification and provides a Stop action.
- Enrollment requires an explicit in-app location consent checkbox and Android location permission.
- Each enrolled device receives its own device token; only its SHA-256 hash is stored in Postgres.
- Device heartbeat and GPS samples are sent to authenticated API routes.
- The Agent maintains a bounded local queue for location samples when network requests fail.
- Reboot restart is opt-in and permission-gated; Android and head-unit OEM restrictions vary.
- Admin Android UI now states that install links have no automatic expiry and can enroll multiple separate devices.
- Latest unified debug APK build: GitHub Actions run `46100596354`, conclusion SUCCESS, commit `e79668f00515f11a76bb342a10fbf375a5f09dd6`.
- Artifact size: 9,179,045 bytes.
- The automated test release asset was refreshed on 2026-11-28:
  https://github.com/ibrahemahmedabdo1-ai/track-car/releases/download/track-car-android-latest/TrackCar.apk
- This is a DEBUG-signed internal test APK, not the production-signed release.

## Production signing — prepared, not yet activated

- This is the first official release; no previous production signing key was reported.
- `apps/trackcar-android/build.gradle.kts` has release signing configuration.
- Manual workflow: `.github/workflows/android-release.yml`.
- The workflow checks the required secrets, verifies the keystore alias, builds the release APK, runs `apksigner verify`, and uploads the APK and SHA-256 checksum.
- Windows helper script: `scripts/windows/create-trackcar-release-keystore.ps1`.
- The helper creates a new 4096-bit RSA JKS signing key and a local Base64 file. The `signing/` folder and signing-key files are ignored by Git.
- The following GitHub Actions repository secrets still must be added by the repository owner:
  1. `TRACKCAR_RELEASE_KEYSTORE_BASE64`
  2. `TRACKCAR_RELEASE_KEYSTORE_PASSWORD`
  3. `TRACKCAR_RELEASE_KEY_ALIAS` (helper default: `trackcar-release`)
  4. `TRACKCAR_RELEASE_KEY_PASSWORD` (same as store password when using the helper)
- Never commit the keystore or send passwords in chat. Back up the keystore and passwords securely in at least two places. Losing the signing key can prevent future app updates.
- The signed-release workflow has not yet run because the signing secrets are not configured.

## Remaining acceptance tests before calling the project complete

1. Generate and safely back up the first official release keystore; add the four GitHub Actions secrets.
2. Run the signed production APK workflow and verify the signature and checksum.
3. Install the APK on at least two physical Android devices using the same reusable invitation link.
4. Verify both devices appear as separate records, each with its own device code, consent, heartbeat, and GPS coordinates.
5. Revoke the shared link; verify new enrollments are rejected while already-enrolled devices remain registered.
6. Test foreground GPS, network loss and queue recovery, stop action, and reboot behavior on the actual target head-unit models.
7. Decide and perform custom-domain migration only after acceptance tests; existing legacy Vercel projects/domains have not been migrated.

## Important scope and safety notes

- No silent installation or hidden location tracking is implemented.
- Android installation requires the device user to confirm the package installer.
- Location sharing requires an in-app user action and Android permissions.
- A successful CI build or API health check does not replace physical-device testing.
