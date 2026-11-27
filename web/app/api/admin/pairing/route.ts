import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { adminAuthorized, json, randomToken, sha256 } from "@/lib/security";
export const runtime = "nodejs";
export async function POST(req: NextRequest) {
  if (!adminAuthorized(req)) return json({error:"Unauthorized"},401);
  let body:any={}; try { body=await req.json(); } catch {}
  const minutes=Math.max(5,Math.min(1440,Number(body.expires_in_minutes)||60));
  const maxUses=Math.max(1,Math.min(20,Number(body.max_uses)||1));
  const code=randomToken(16);
  const expires=new Date(Date.now()+minutes*60000);
  try {
    await db().query(
      "INSERT INTO public.pairing_invites(token_hash,expires_at,max_uses,label) VALUES($1,$2,$3,$4)",
      [sha256(code),expires,maxUses,typeof body.label==="string"?body.label.slice(0,120):"Fleet device enrollment"]
    );
    return json({pairing_code:code,expires_at:expires.toISOString(),max_uses:maxUses});
  } catch { return json({error:"Could not create pairing code"},500); }
}