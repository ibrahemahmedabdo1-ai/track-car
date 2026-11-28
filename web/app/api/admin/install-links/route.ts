import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { adminAuthorized, json, randomToken, sha256 } from "@/lib/security";
export const runtime = "nodejs";

const APP_URL = process.env.NEXT_PUBLIC_APP_URL || "https://track-car-platform-git-main-otpprovider.vercel.app";

export async function POST(req: NextRequest) {
  if (!adminAuthorized(req)) return json({ error: "Unauthorized" }, 401);
  let body: any = {};
  try { body = await req.json(); } catch {}
  const label = typeof body.label === "string" && body.label.trim()
    ? body.label.trim().slice(0, 120)
    : "Track Car vehicle installation";
  const token = randomToken(32);
  try {
    // NULL expiry and NULL max_uses mean this link remains active until Admin revokes it.
    // Each successful enrollment creates a separate device record and its own device token.
    await db().query(
      "INSERT INTO public.pairing_invites(token_hash,expires_at,max_uses,label) VALUES($1,NULL,NULL,$2)",
      [sha256(token), label]
    );
    return json({
      install_url: APP_URL + "/install/" + token,
      expires_at: null,
      max_uses: null,
      multi_device: true,
      note: "This link has no automatic expiry and can enroll multiple separate devices until Admin revokes it."
    }, 201);
  } catch {
    return json({ error: "Could not create installation link" }, 500);
  }
}

export async function DELETE(req: NextRequest) {
  if (!adminAuthorized(req)) return json({ error: "Unauthorized" }, 401);
  let body: any = {};
  try { body = await req.json(); } catch {}
  const token = String(body.token || "").trim();
  if (token.length < 40) return json({ error: "A valid installation token is required" }, 400);
  try {
    const result = await db().query(
      "UPDATE public.pairing_invites SET revoked_at=now() WHERE token_hash=$1 AND revoked_at IS NULL RETURNING id",
      [sha256(token)]
    );
    if (!result.rowCount) return json({ error: "Invitation not found or already revoked" }, 404);
    return json({ revoked: true });
  } catch {
    return json({ error: "Could not revoke installation link" }, 500);
  }
}
