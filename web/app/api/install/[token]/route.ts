import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { json, sha256 } from "@/lib/security";
export const runtime = "nodejs";
export async function GET(_req: NextRequest, context: { params: Promise<{ token: string }> }) {
  const { token } = await context.params;
  if (!/^[a-f0-9]{64}$/i.test(token)) return json({ valid: false, error: "Invalid installation link" }, 404);
  try {
    const result = await db().query(
      "SELECT label,expires_at,max_uses,used_count FROM public.pairing_invites WHERE token_hash=$1 AND revoked_at IS NULL AND (expires_at IS NULL OR expires_at>now()) AND (max_uses IS NULL OR used_count<max_uses) LIMIT 1",
      [sha256(token)]
    );
    if (!result.rowCount) return json({ valid: false, error: "This installation link has been revoked, expired or reached its enrollment limit." }, 410);
    return json({
      valid: true,
      label: result.rows[0].label,
      expires_at: result.rows[0].expires_at,
      multi_device: result.rows[0].max_uses === null,
      unlimited: result.rows[0].expires_at === null && result.rows[0].max_uses === null
    });
  } catch (error) {
    console.error("trackcar_install_invite_validation_failed", error);
    return json({ valid: false, error: "Installation service is temporarily unavailable" }, 503);
  }
}
