# Track Car unified Android app and installation links

## Unified application
The `apps/trackcar-android` application compiles the existing Admin, Vehicle Agent and Diagnostics source trees into one Android package: `com.trackcar.app`. The original app modules remain in the repository as source modules and are not deleted.

The launcher presents three workspaces. The Admin workspace still requires the server-side admin key. The vehicle workspace requires explicit location consent and Android runtime permissions. Location service operation remains visible through Android's foreground-service notification.

## Installation links
An administrator-generated installation link is a time-limited, one-use enrollment invitation. It is not an admin API key. The invitation token is stored only as a SHA-256 hash in the database and is consumed by the existing enrollment endpoint. The installer must still confirm APK installation in Android's package installer.

## Release status
GitHub Actions produces a debug APK for test installation. A production APK must be signed with an organization-controlled release keystore stored as GitHub Actions secrets. Do not publish a debug build as a production release.