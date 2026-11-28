"use client";
import { use, useEffect, useState } from "react";

const APK_URL = "https://github.com/ibrahemahmedabdo1-ai/track-car/releases/download/track-car-android-latest/TrackCar.apk";

export default function InstallPage({ params }: { params: Promise<{ token: string }> }) {
  const { token } = use(params);
  const [state, setState] = useState<"loading"|"valid"|"invalid">("loading");
  const [message, setMessage] = useState("Checking your secure installation invitation…");
  useEffect(() => {
    fetch("/api/install/" + encodeURIComponent(token), { cache: "no-store" })
      .then(async r => { const d = await r.json(); if (!r.ok || !d.valid) throw new Error(d.error || "Invitation is not valid"); setState("valid"); setMessage("Installation link for " + (d.label || "your fleet") + " is active. It can be used to enroll multiple separate devices and remains active until your fleet administrator revokes it. Download Track Car, install it using Android's installer, then open this page again and select Open Track Car to continue enrollment."); })
      .catch(e => { setState("invalid"); setMessage(e instanceof Error ? e.message : "Could not validate invitation"); });
  }, [token]);
  return <main className="shell" style={{maxWidth:620}}>
    <div className="brand">TRACK CAR</div>
    <section className="panel" style={{marginTop:28}}>
      <h1>Install Track Car</h1>
      <p className="muted">{message}</p>
      {state === "valid" && <>
        <a className="btn" style={{display:"inline-block",textDecoration:"none",marginTop:12}} href={APK_URL} rel="noreferrer" referrerPolicy="no-referrer">Download Track Car test APK</a>
        <p className="small" style={{color:"#f5c66b"}}>Test build only: this APK is debug-signed, not a production release. A company-owned release signing key is required before distributing production updates.</p>
        <p className="muted small">Android will ask you to confirm installation. If prompted, allow this browser to install apps, then confirm the package installer.</p>
        <a className="btn secondary" style={{display:"inline-block",textDecoration:"none",marginTop:8}} href={"trackcar://install/" + token}>Open Track Car after installation</a>
        <p className="muted small">The invitation has no automatic expiry and is reusable for multiple devices. Each device is registered independently, and enrollment starts only after that device's user explicitly approves location sharing in the app. Admin can revoke the link at any time.</p>
      </>}
      {state === "loading" && <p className="tag">Validating</p>}
      {state === "invalid" && <p className="error">Ask your fleet administrator to create or reactivate an installation link.</p>}
    </section>
  </main>;
}
