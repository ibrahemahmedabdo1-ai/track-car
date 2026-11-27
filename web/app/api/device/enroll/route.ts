import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { json, randomToken, sha256 } from "@/lib/security";
export const runtime = "nodejs";
export async function POST(req:NextRequest) {
  let b:any={}; try { b=await req.json(); } catch { return json({error:"Invalid JSON"},400); }
  const code=String(b.pairing_code||"").trim();
  const label=String(b.device_label||"").trim().slice(0,120);
  const info=b.device_info&&typeof b.device_info==="object"?b.device_info:{};
  if(code.length<20||!label) return json({error:"A valid pairing code and device label are required"},400);
  if(b.location_consent!==true) return json({error:"Location consent is required before enrollment"},403);
  const client=await db().connect();
  try {
    await client.query("BEGIN");
    const invite=await client.query(
      "UPDATE pairing_invites SET used_count=used_count+1 WHERE token_hash=$1 AND revoked_at IS NULL AND expires_at>now() AND used_count<max_uses RETURNING id,label",
      [sha256(code)]
    );
    if(!invite.rowCount){await client.query("ROLLBACK");return json({error:"Pairing code is invalid, expired, revoked or already used"},400);}
    const token=randomToken(32);
    const deviceCode="TC-"+randomToken(5).toUpperCase();
    const manufacturer=String(info.manufacturer||"Unknown").slice(0,120);
    const model=String(info.model||"Unknown").slice(0,120);
    const result=await client.query(
      "INSERT INTO devices(device_code,label,platform,status,api_token_hash,manufacturer,model,device_codename,android_release,sdk_int,build_fingerprint,security_patch,cpu_abis,agent_version,capabilities,enrollment_state,consent_at,last_seen_at) VALUES($1,$2,'android','online',$3,$4,$5,$6,$7,$8,$9,$10,$11::jsonb,$12,$13::jsonb,'enrolled',now(),now()) RETURNING id,device_code",
      [deviceCode,label,sha256(token),manufacturer,model,String(info.codename||"").slice(0,120),String(info.android_release||"").slice(0,40),Number(info.sdk_int)||null,String(info.build_fingerprint||"").slice(0,300),String(info.security_patch||"").slice(0,40),JSON.stringify(Array.isArray(info.cpu_abis)?info.cpu_abis.slice(0,8):[]),String(info.agent_version||"1.0.0").slice(0,40),JSON.stringify({gps:true,background_location:true})]
    );
    await client.query(
      "INSERT INTO device_consents(device_id,consent_type,consent_version,granted,notice_text) VALUES($1,'location','1.0',true,$2)",
      [result.rows[0].id,"Authorized fleet vehicle location tracking was enabled during device enrollment."]
    );
    await client.query(
      "INSERT INTO device_events(device_id,event_type,event_payload) VALUES($1,'device_enrolled',$2::jsonb)",
      [result.rows[0].id,JSON.stringify({manufacturer,model,android_release:info.android_release||null})]
    );
    await client.query("COMMIT");
    return json({device_id:result.rows[0].id,device_code:result.rows[0].device_code,device_token:token,api_base_url:process.env.NEXT_PUBLIC_API_BASE_URL||""});
  } catch(e) {
    await client.query("ROLLBACK");
    return json({error:"Enrollment failed"},500);
  } finally { client.release(); }
}