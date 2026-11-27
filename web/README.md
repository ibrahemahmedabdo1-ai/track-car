# Track Car Platform

Next.js API and fleet console deployed separately from the Android Gradle modules.

## Required server environment
- DATABASE_URL: least-privilege Neon connection string for the Track Car development branch.
- TRACKCAR_ADMIN_KEY: server-side administrator key. Never expose it in NEXT_PUBLIC variables.

## Routes
- GET /api/health
- POST /api/admin/pairing (x-admin-key)
- GET /api/admin/devices (x-admin-key)
- POST /api/device/enroll (pairing_code, device_label, device_info, location_consent=true)
- POST /api/device/heartbeat (Bearer device token)
- POST /api/device/locations (Bearer device token)
- GET/POST /api/device/commands (Bearer device token)

Device tokens are returned once at enrollment and only their SHA-256 hashes are stored in Postgres. Pairing codes are random, expire, are single-use by default, and are stored as hashes. Location data is accepted only from enrolled devices.
