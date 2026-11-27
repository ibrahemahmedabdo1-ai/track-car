# Track Car Implementation Status

Last updated: 2026-11-27

## GitHub
Repository: `ibrahemahmedabdo1-ai/track-car`, branch: `main`.

Implemented foundations:
- Existing Admin Android module retained.
- Added Installer Android module.
- Added Vehicle Agent Android module.
- Added Android CI workflow to build all three debug APKs.
- Added multi-device architecture, compatibility and API contract documentation.
- Added additive SQL migration `backend/db/migrations/0001_multi_device_foundation.sql`.

## Vercel
Existing projects and assigned Vercel domains:
- `track-car`: `track-car-phi.vercel.app`
- `track-car-installer`: `track-car-installer.vercel.app`

The existing Track Car web/API deployment is separate from the Android repository at present. Do not overwrite or reassign its production domain until the Git-linked replacement has passed build and smoke tests.

## Neon
Existing project: `track-car` (`silent-shape-33906165`), region `aws-us-east-2`, PostgreSQL 17.

The original `production` branch was archived. A separate development branch named `track-car-dev-bootstrap` was created from it (`br-bold-sky-b5zque6o`). The additive multi-device migration was applied and verified on this development branch only. The archived source branch and existing production Vercel environment variables were not modified.

## Not production-ready yet
- Device enrollment and authentication API.
- Admin login and role enforcement review.
- Agent GPS collection, foreground service, offline queue and sync.
- Installer signed-release verification and Agent handoff.
- Vercel-to-Neon connection using a verified active branch.
- Device compatibility testing on physical head units.
- Remote support, screen viewing and control.

## Next implementation sequence
1. Confirm and stabilize the Neon production branch without losing existing data.
2. Connect the Vercel API to a tested database branch using encrypted environment variables.
3. Implement enrollment, device heartbeat and diagnostics endpoints.
4. Implement consent-based Agent location service and offline sync.
5. Connect Admin device/vehicle views to live API data.
6. Publish signed release artifacts and verify installer download/update flow.
