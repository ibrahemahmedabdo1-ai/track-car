# Track Car deployment activation

The preview application has been deployed at:

https://track-car-platform-git-main-otpprovider.vercel.app

Vercel project: `track-car-platform`  
Root directory: `web`  
Neon project: `silent-shape-33906165`  
Neon branch: `track-car-dev-bootstrap`  
Database: `trackcar`  
Least-privilege database role: `trackcar_api`

## Two server-only variables

Open Vercel → track-car-platform → Settings → Environment Variables. Add both variables to Preview, Production and Development:

1. `DATABASE_URL`: copy the connection string from Neon Console for project `track-car`, branch `track-car-dev-bootstrap`, database `trackcar`, role `trackcar_api`. Use the pooled connection string if available.
2. `TRACKCAR_ADMIN_KEY`: a randomly generated secret with at least 32 random bytes.

Do not prefix either variable with `NEXT_PUBLIC_`. Never put them in GitHub or the Android app.

After saving, redeploy the latest `main` deployment. Confirm:

- `GET /api/health` returns `ok: true` and `database: connected`.
- Open the fleet console, enter the administrator key and create a one-use pairing code.
- Enter the pairing code in Vehicle Agent, confirm the location notice and grant Android location permission.
- The device should appear in the fleet console and begin sending GPS points.

## Important
The current Vercel project is a preview/API deployment. The existing Track Car website domains were deliberately left attached to their original projects until the new system passes live tests.
