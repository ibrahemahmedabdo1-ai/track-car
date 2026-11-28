import { createHash, randomBytes, timingSafeEqual } from "crypto";
import { NextRequest, NextResponse } from "next/server";
export const sha256 = (value: string) => createHash("sha256").update(value).digest("hex");
export const randomToken = (bytes = 32) => randomBytes(bytes).toString("hex");
export const json = (data: unknown, status = 200) => NextResponse.json(data, { status, headers: { "Cache-Control": "no-store, max-age=0" } });
export function adminAuthorized(req: NextRequest) {
  const expected = process.env.TRACKCAR_ADMIN_KEY || "";
  const actual = req.headers.get("x-admin-key") || "";
  if (!expected || expected.length !== actual.length) return false;
  return timingSafeEqual(Buffer.from(expected), Buffer.from(actual));
}
export async function deviceFromRequest(req: NextRequest) {
  const header = req.headers.get("authorization") || "";
  const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
  if (!token || token.length < 40) return null;
  const { db } = await import("./db");
  const result = await db().query(
    "SELECT id, status FROM devices WHERE api_token_hash = $1 AND status <> 'revoked' LIMIT 1",
    [sha256(token)]
  );
  return result.rows[0] || null;
}