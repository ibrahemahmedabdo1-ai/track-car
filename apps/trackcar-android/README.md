# Track Car unified Android app

Build with `gradle :apps:trackcar-android:assembleDebug`.

The single APK compiles the Admin, Vehicle Agent and Diagnostics source sets. On first launch, choose the workspace that matches your role. The vehicle workspace requires location consent and Android permissions; its foreground location notification remains visible.

Installation invitations are one-use pairing credentials. They do not bypass Android's package installer or install silently.