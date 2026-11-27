"use client";
import { useEffect, useState } from "react";

const APK_URL = "https://github.com/ibrahemahmedabdo1-ai/track-car/releases/download/track-car-android-latest/TrackCar.apk";

export default function InstallPage({ params }: { params: { token: string } }) {
  const [state, setState] = useState<"loading"|"valid"|"invalid">("loading");
  const [message, setMessage] = useState("Checking your secure installation invitation…");
  useEffect(() => {
    fetch("/api/install/" + encodeURIComponent(params.token), { cache: "no-store" })
      .then(async r => { const d = await r.json(); if (!r.ok || !d.valid) throw new Error(d.error || "Invitation is not valid"); setState("valid"); setMessage("Your one-time invitation is ready. Download Track Car, install it using Android's installer, then open this page again and select Open Track Car to continue enrollment."); })
      .catch(e => { setState("invalid"); setMessage(e instanceof Error ? e.message : "Could not validate invitation"); });
  }, [params.token]);
  return <main className="shell" style={{maxWidth:620}}>
    <div className="brand">TRACK CAR</div>
    <section className="panel" style={{marginTop:28}}>
      <h1>Install Track Car</h1>
      <p className="muted">{message}</p>
      {state === "valid" && <>
        <a className="btn" style={{display:"inline-block",textDecoration:"none",marginTop:12}} href={APK_URL}>Download Android APK</a>
        <p className="muted small">Android will ask you to confirm installation. If prompted, allow this browser to install apps, then confirm the package installer.</p>
        <a className="btn secondary" style={{display:"inline-block",textDecoration:"none",marginTop:8}} href={"trackcar://install/" + params.token}>Open Track Car after installation</a>
        <p className="muted small">The invitation is one-use and expires automatically. Enrollment starts only after you explicitly approve location sharing in the app.</p>
      </>}
      {state === "loading" && <p className="tag">Validating</p>}
      {state === "invalid" && <p className="error">Ask your fleet administrator to create a new installation link.</p>}
    </section>
  </main>;
}
