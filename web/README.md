# Track Car Platform

Next.js API and fleet console deployed separately from the Android Gradle modules.

## Required server environment
- DATABASE_URL: least-privilege Neon connection string for the Track Car production branch.
- TRACKCAR_ADMIN_KEY: server-side administrator key. Never expose it in NEXT_PUBLIC variables.

## Routes
- GET /api/health
- GET /api/admin/install-links (x-admin-key): list recent installation links, revocation state, and enrolled-device counts (raw tokens are never returned).
- POST /api/admin/install-links (x-admin-key): create a reusable installation link with no automatic expiry and unlimited device enrollments until revoked; the full URL is returned only once.
- DELETE /api/admin/install-links (x-admin-key): revoke a link by its invitation ID; older clients may still revoke by raw token.
- POST /api/admin/pairing (x-admin-key): create a reusable pairing code with the same no-expiry, unlimited-device behavior.
- DELETE /api/admin/install-links (x-admin-key): revoke an installation link.
- GET /api/install/[token]: validate an installation link.
- GET /api/admin/devices (x-admin-key)
- POST /api/device/enroll (pairing_code, device_label, device_info, location_consent=true)
- POST /api/device/heartbeat (Bearer device token)
- POST /api/device/locations (Bearer device token)
- GET/POST /api/device/commands (Bearer device token)

Device tokens are returned once at enrollment and only their SHA-256 hashes are stored in Postgres. Every enrollment creates a separate device row and a separate device token. Reusable install-link tokens are stored as hashes; they remain active until explicitly revoked. Every device user must separately grant location consent and Android permissions.
