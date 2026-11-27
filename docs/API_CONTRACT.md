# Track Car API Contract (v1)

All endpoints are HTTPS-only and require a short-lived enrollment credential or an authenticated user/device session. This document is a contract target; the API is not yet deployed.

## Device enrollment
- `POST /api/v1/enrollment/start`: validate a one-time enrollment code and return a short-lived challenge.
- `POST /api/v1/enrollment/complete`: exchange the challenge for a device-bound credential after device details and required user disclosure are confirmed.
- Device credentials are stored hashed; raw credentials are shown only once and can be revoked.

## Device operations
- `POST /api/v1/devices/heartbeat`: report app version, Android release, SDK, battery/charging state, connectivity and capability flags.
- `GET /api/v1/devices/config`: retrieve the signed, versioned device configuration.
- `POST /api/v1/locations/batch`: submit timestamped, accuracy-labelled GPS points while tracking is enabled.
- `POST /api/v1/commands/{commandId}/ack`: acknowledge an allowlisted command and report its result.

## Releases
- `GET /api/v1/releases/{app}/latest`: return version, minimum supported SDK, download URL, SHA-256 checksum and signing-certificate fingerprint.
- Clients must verify checksum and package signature before installation or update.

## Required controls
- Organization and vehicle authorization on every request.
- Per-device rate limits, replay protection, idempotency keys and clock-skew tolerance.
- No remote shell or arbitrary command execution.
- Location retention and access are policy-controlled and auditable.
- Remote screen access is opt-in, session-scoped and visibly indicated on the device.
