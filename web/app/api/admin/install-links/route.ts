import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { adminAuthorized, json, randomToken, sha256 } from "@/lib/security";
export const runtime = "nodejs";

const APP_URL = process.env.NEXT_PUBLIC_APP_URL || "https://track-car-platform-git-main-otpprovider.vercel.app";

export async function GET(req: NextRequest) {
  if (!adminAuthorized(req)) return json({ error: "Unauthorized" }, 401);
  try {
    const result = await db().query(
      `SELECT pi.id,pi.label,pi.created_at,pi.revoked_at,pi.expires_at,pi.max_uses,
        (COUNT(DISTINCT de.device_id) FILTER (WHERE de.event_type='device_enrolled'))::int AS enrolled_devices
       FROM public.pairing_invites pi
       LEFT JOIN public.device_events de ON de.event_payload->>'invite_id'=pi.id::text
       GROUP BY pi.id
       ORDER BY pi.created_at DESC
       LIMIT 100`
    );
    return json({ links: result.rows });
  } catch (error) {
    console.error("trackcar_admin_install_links_list_failed", error);
    return json({ error: "Could not load installation links" }, 500);
  }
}

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
    const result = await db().query(
      "INSERT INTO public.pairing_invites(token_hash,expires_at,max_uses,label) VALUES($1,NULL,NULL,$2) RETURNING id",
      [sha256(token), label]
    );
    return json({
      invite_id: result.rows[0].id,
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
  const inviteId = String(body.invite_id || "").trim();
  const token = String(body.token || "").trim();
  try {
    let result;
    if (/^[0-9a-f-]{36}$/i.test(inviteId)) {
      result = await db().query(
        "UPDATE public.pairing_invites SET revoked_at=now() WHERE id=$1::uuid AND revoked_at IS NULL RETURNING id",
        [inviteId]
      );
    } else if (/^[a-f0-9]{64}$/i.test(token)) {
      // Backward compatibility for older Admin clients that still revoke by raw token.
      result = await db().query(
        "UPDATE public.pairing_invites SET revoked_at=now() WHERE token_hash=$1 AND revoked_at IS NULL RETURNING id",
        [sha256(token)]
      );
    } else {
      return json({ error: "A valid invitation ID is required" }, 400);
    }
    if (!result.rowCount) return json({ error: "Invitation not found or already revoked" }, 404);
    return json({ revoked: true, invite_id: result.rows[0].id });
  } catch {
    return json({ error: "Could not revoke installation link" }, 500);
  }
}
