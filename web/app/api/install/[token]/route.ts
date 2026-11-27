import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { json, sha256 } from "@/lib/security";
export const runtime = "nodejs";
export async function GET(_req: NextRequest, context: { params: Promise<{ token: string }> }) {
  const { token } = await context.params;
  if (!/^[a-f0-9]{64}$/i.test(token)) return json({ valid: false, error: "Invalid installation link" }, 404);
  try {
    const result = await db().query(
      "SELECT label,expires_at,max_uses,used_count FROM pairing_invites WHERE token_hash=$1 AND revoked_at IS NULL AND expires_at>now() AND used_count<max_uses LIMIT 1",
      [sha256(token)]
    );
    if (!result.rowCount) return json({ valid: false, error: "This installation link has expired, been revoked or already been used." }, 410);
    return json({ valid: true, label: result.rows[0].label, expires_at: result.rows[0].expires_at });
  } catch {
    return json({ valid: false, error: "Installation service is temporarily unavailable" }, 503);
  }
}
