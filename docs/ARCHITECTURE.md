# Track Car — Architecture Baseline

## Product goal
A single fleet-management platform for Android head units from multiple manufacturers and model families. The website, installer, vehicle agent, and admin app share one backend and one device registry.

## Deployment domains
- Admin/web platform: `track-car-phi.vercel.app` (existing Vercel project domain).
- Installer entry point: `track-car-installer.vercel.app` (existing Vercel project domain).
- These are Vercel-provided domains; no custom domain is assumed.

## Components
1. **Web/API (Vercel):** enrollment, authentication, device registry, configuration, trip ingestion, audit events, installer downloads and version metadata.
2. **Database (Neon Postgres):** organizations, users, vehicles, devices, enrollment tokens, locations, trips, commands, audit logs and app releases.
3. **Installer (Android):** device diagnostics, compatibility profile, signed package verification, and guided installation.
4. **Vehicle Agent (Android):** consent-aware enrollment, location collection, foreground service, offline queue and secure sync.
5. **Admin Android:** fleet overview, device health, vehicle assignments, trips, alerts and audited remote support.

## Multi-device compatibility
The Android platform is shared, but capabilities vary by vendor, firmware, Android release, bootloader state and installed root manager. Use capability detection and versioned device profiles. Never flash a generic boot image or assume one root package fits all devices.

## Root and installation constraints
- A web link cannot silently install an APK or grant root on stock Android.
- A PDF or browser page cannot install root.
- Root support is an optional capability, not a universal prerequisite for ordinary tracking.
- Root-dependent operations are enabled only after explicit authorization and a verified compatible method.
- If root is unavailable, use normal Android installation and supported permissions; show the user the required system prompts.
- Do not exploit unknown vulnerabilities, hide the agent, or bypass Android consent/security dialogs.

## Security baseline
- HTTPS everywhere; short-lived enrollment tokens; device-bound credentials with rotation.
- Signed APKs and verified release checksums.
- Least-privilege permissions; explicit location disclosure and consent.
- Foreground-service notification for active tracking where Android requires it.
- Role-based access, command allowlists, rate limits, audit trails and revocation.
- Remote viewing/control is disabled by default and must be visible, authorized, and unavailable while the vehicle is moving.

## Current implementation status
The repository currently contains an initial Admin Android shell and CI build. Installer and Agent modules are being introduced as foundations. Backend, database migrations, production authentication, device enrollment, GPS ingestion and live maps are not yet complete.
