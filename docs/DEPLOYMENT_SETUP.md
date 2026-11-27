# Track Car deployment and release setup

Last verified: 2026-11-27

## Live production website and API

- Vercel project: `track-car-platform`
- Root directory: `web`
- Production URL: https://track-car-platform-git-main-otpprovider.vercel.app
- Production deployment: `dpl_2YC9bL8Vr77Vzb1xZ2tX5m3pcs4g`
- Deployed Git commit: `ac5bbfc753935b6323a9e181c9fb93eece4d2337`
- Neon project: `silent-shape-33906165`
- Production branch: `production` (`br-lively-sea-b5o9hotj`)
- Database: `trackcar`
- Runtime database role: `trackcar_runtime`

The deployment completed successfully. Live `GET /api/health` returned HTTP 200 with `ok: true` and `database: connected`.

## Vercel environment variables

Keep all values server-side. Do not prefix secrets or database URLs with `NEXT_PUBLIC_`.

- `TRACKCAR_DATABASE_URL`: pooled or direct Neon connection string for the production branch and runtime role `trackcar_runtime`. The application prefers this variable over `DATABASE_URL`.
- `DATABASE_URL`: retained as a fallback for existing environments.
- `TRACKCAR_ADMIN_KEY`: strong random administrator key. Never include it in the APK or source control.
- `NEXT_PUBLIC_APP_URL`: optional. If omitted, install links use the stable production alias above.

The production health route and invitation-validation route were checked after deployment. A temporary one-use invitation was inserted for the validation test and removed afterward.

## Android test build

The unified Android application is built by GitHub Actions:

- Workflow: `.github/workflows/android-admin.yml`
- Latest successful unified APK build: run `45999155618`
- Public test APK: https://github.com/ibrahemahmedabdo1-ai/track-car/releases/download/track-car-android-latest/TrackCar.apk

This is a DEBUG-signed internal test APK. Do not distribute it as a production release.

## Signed production APK

A manual release workflow is prepared at `.github/workflows/android-release.yml`. Before running it, add these four GitHub Actions repository secrets:

1. `TRACKCAR_RELEASE_KEYSTORE_BASE64` — Base64-encoded Android release keystore.
2. `TRACKCAR_RELEASE_KEYSTORE_PASSWORD` — keystore password.
3. `TRACKCAR_RELEASE_KEY_ALIAS` — signing key alias.
4. `TRACKCAR_RELEASE_KEY_PASSWORD` — key password.

Never commit the keystore or passwords to the repository. Preserve the same keystore for every future update; losing it can prevent upgrades of the installed app.

After adding all four secrets, run **Track Car Signed Production APK** from GitHub Actions using workflow_dispatch. It uploads `TrackCar-release.apk` as an artifact. The release workflow does not publish a public release automatically.

## Vehicle installation flow

1. Admin creates an installation link and optionally labels the vehicle.
2. The link expires automatically (default 24 hours) and is single-use.
3. The recipient downloads the APK and confirms installation in Android.
4. The recipient opens the invitation link again and selects Open Track Car.
5. The app passes the invite token to Vehicle Agent. The driver explicitly checks the location-consent box and grants Android permissions.
6. The backend consumes the invitation during enrollment and issues a device token.

Admin can copy, share, and revoke a generated link. Android installation always requires the device user to confirm; this is not a silent installer.

## Remaining real-device checks

- Install the APK on the actual vehicle head unit and confirm the invite deep link opens the unified app.
- Complete a real enrollment and verify the device appears in Admin.
- Confirm foreground GPS updates, battery heartbeat, offline queue recovery, and stop control.
- Test background/reboot behavior on each target head-unit model. Android/OEM policies vary.
- Add and protect the production signing keystore before distributing release updates.

Existing custom domains and older Vercel projects have not been migrated.
