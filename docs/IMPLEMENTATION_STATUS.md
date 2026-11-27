# Track Car Implementation Status

Last updated: 2026-11-27

## Delivered in this implementation pass

### Android
- Installer, Admin and Vehicle Agent modules are included in the Gradle project.
- Installer and Agent target Android API 21+ and use automatic device profile collection for compatibility decisions.
- Vehicle Agent uses Android LocationManager, avoiding a Google Play Services dependency on Android head units.
- Foreground location service displays a persistent Android notification and provides a stop action.
- Enrollment uses a one-time pairing code, explicit location consent, and a device token returned once.
- Device token is stored on-device; only its SHA-256 hash is stored server-side.
- GPS samples are sent to the API with authenticated device-token requests. The app retains a bounded local queue when the network is unavailable.
- Heartbeat includes battery and Agent version.
- Reboot restart is opt-in and permission-gated. Android/OEM policies may still prevent restart on some models.

### Backend
- Added Next.js API and fleet console under `web/`.
- Added health, admin pairing, admin device listing, device enrollment, heartbeat, location batch, and command polling/result endpoints.
- Pairing codes expire and are single-use by default; only hashes are stored.
- API database role `trackcar_api` was created with read/write access to the public schema on the development branch.
- The API has been designed for the Track Car development branch; production data remains untouched.

### Database
- Neon project: `silent-shape-33906165`.
- Development branch: `track-car-dev-bootstrap` (`br-bold-sky-b5zque6o`).
- Existing vehicle, device, location, pairing, consent, command, event, release and compatibility tables are present.
- Confirmed the least-privilege API role can read the relevant tables.
- Original archived branch remains unchanged.

### Vercel
- New project: `track-car-platform` (`prj_PMOFbNBV88AMauHKuArOgXV4APJT`).
- Root directory: `web`; framework: Next.js; Node.js: 22.x.
- Preview deployment built successfully:
  `https://track-car-platform-git-main-otpprovider.vercel.app`
- Existing `track-car` and `track-car-installer` projects and their domains were not changed.
- The new project's SSO preview protection was disabled so the device API can be reached; the API itself requires admin/device authentication.

## Required activation step
The new Vercel project still needs two encrypted environment variables before the database-backed API can pass its health check:
- `DATABASE_URL`: Neon connection string for `track-car-dev-bootstrap`, using role `trackcar_api`.
- `TRACKCAR_ADMIN_KEY`: a strong random 32-byte administrator secret.

The two server-only values have not yet been added to this new Vercel project. Add them directly in Vercel Project Settings → Environment Variables to keep credentials out of source control and the browser, then redeploy.

## Verification still outstanding
- Android CI passed for commit `ee2be9daabcf8952ed4a37afd4848c6eeb8d8bce`; the Admin, Installer and Agent debug APK artifact was uploaded.
- Add the two Vercel secrets and verify `GET /api/health` returns database connected.
- Perform a real pairing/enrollment/GPS sync using a physical Android head unit.
- Validate automatic restart and background behavior per OEM. Android 12+ and vendor firmware can restrict this.
- Production domain migration is intentionally not performed until the new preview and real-device test pass.

## Live activation verification — 2026-11-27

- Both required server-only environment variables, `DATABASE_URL` and `TRACKCAR_ADMIN_KEY`, are now present in the Vercel Production environment.
- Latest deployment for `track-car-platform` is READY:
  - Deployment: `dpl_8n3Dowpnp5SPyFMUmZybzpRFXt5e`
  - Commit: `184d7f1978bb15fa79c65b610473cf54ddbaa319`
  - URL: `https://track-car-platform-git-main-otpprovider.vercel.app`
- Live `GET /api/health` returned HTTP 200 with `ok: true` and `database: connected`.
- Vercel runtime scan (last 24 hours) found one warning group on `/api/health`: pg-connection-string SSL-mode compatibility warning. It was not a database connection failure.
- The API's authenticated admin/device workflows and a physical Android pairing/GPS upload have NOT yet been end-to-end verified.
- Environment variables are confirmed in Production only; Preview and Development still need separate values if those environments are to be tested.
- No custom production domain migration has been performed. Existing Track Car domains remain attached to their previous projects.
- APKs remain debug builds for internal testing, not production-signed releases.


## Completion pass — 2026-11-27

- Fixed the JSX syntax error in `web/app/install/[token]/page.tsx` that caused Vercel build `dpl_35FL34ugU2HBcEjUtisywvHuP3WF` to fail.
- Added `.github/workflows/web-build.yml`; GitHub Actions web build passed on commit `ac5bbfc753935b6323a9e181c9fb93eece4d2337` (run `45999733206`).
- Deployed production commit `ac5bbfc753935b6323a9e181c9fb93eece4d2337`; Vercel deployment `dpl_2YC9bL8Vr77Vzb1xZ2tX5m3pcs4g` reached READY and owns the stable production aliases.
- Live health check returned HTTP 200, `ok: true`, and `database: connected`.
- Added a dedicated Neon runtime role `trackcar_runtime` and configured server-only `TRACKCAR_DATABASE_URL` in Vercel Production. Runtime queries use the production branch and explicit `public` table names.
- Live invitation validation was tested with a temporary one-use invitation; the API returned valid metadata and the temporary record was removed afterward.
- The unified Android APK build passed in GitHub Actions run `45999155618`. The latest test APK is available from the GitHub release.
- Added a manual signed-release workflow and Gradle release signing configuration. A production-signed APK is still pending the four repository secrets listed in `docs/DEPLOYMENT_SETUP.md`.
- Still outstanding: physical head-unit installation, actual enrollment and GPS end-to-end test, OEM-specific background/reboot validation, and custom-domain migration.

- Follow-up Android build run `46002056975` passed on commit `70846adeba6f095672356375119426f32453c1e8`; it includes repeat-install deep-link handling and the updated test APK was published to the existing GitHub release.
- Added `singleTop` launch mode so repeat invitation links can reach the existing launcher activity; `onNewIntent` forwards the invitation token to Vehicle Agent.
- Added `.github/workflows/android-release.yml` for manually building a signed production APK. Its keystore path resolves from the repository root; it remains gated on the four GitHub Actions secrets.
