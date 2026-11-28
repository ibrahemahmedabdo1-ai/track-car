import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { adminAuthorized, json, randomToken, sha256 } from "@/lib/security";
export const runtime = "nodejs";
export async function POST(req: NextRequest) {
  if (!adminAuthorized(req)) return json({ error: "Unauthorized" }, 401);
  let body: any = {};
  try { body = await req.json(); } catch {}
  const label = typeof body.label === "string" && body.label.trim()
    ? body.label.trim().slice(0, 120)
    : "Fleet device enrollment";
  const code = randomToken(32);
  try {
    // The pairing-code API follows the same reusable, no-expiry policy as Admin install links.
    await db().query(
      "INSERT INTO public.pairing_invites(token_hash,expires_at,max_uses,label) VALUES($1,NULL,NULL,$2)",
      [sha256(code), label]
    );
    return json({ pairing_code: code, expires_at: null, max_uses: null, multi_device: true });
  } catch {
    return json({ error: "Could not create pairing code" }, 500);
  }
}
