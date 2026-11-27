import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { deviceFromRequest, json } from "@/lib/security";
export const runtime = "nodejs";
export async function GET(req:NextRequest) {
  const device=await deviceFromRequest(req);if(!device)return json({error:"Unauthorized device"},401);
  try{
    const result=await db().query(
      "UPDATE device_commands SET status='delivered' WHERE id IN (SELECT id FROM device_commands WHERE device_id=$1 AND status='queued' AND expires_at>now() ORDER BY requested_at ASC LIMIT 20 FOR UPDATE SKIP LOCKED) RETURNING id,command_type,payload,expires_at",
      [device.id]
    );
    return json({commands:result.rows});
  }catch{return json({error:"Could not fetch commands"},500)}
}
export async function POST(req:NextRequest) {
  const device=await deviceFromRequest(req);if(!device)return json({error:"Unauthorized device"},401);
  let b:any={};try{b=await req.json()}catch{return json({error:"Invalid JSON"},400)}
  const id=String(b.command_id||"");const status=["acknowledged","completed","failed"].includes(b.status)?b.status:"acknowledged";
  if(!id)return json({error:"command_id is required"},400);
  try{
    const r=await db().query("UPDATE device_commands SET status=$3,acknowledged_at=CASE WHEN $3='acknowledged' THEN now() ELSE acknowledged_at END,completed_at=CASE WHEN $3 IN ('completed','failed') THEN now() ELSE completed_at END,result=$4::jsonb WHERE id=$1 AND device_id=$2 RETURNING id",[id,device.id,status,JSON.stringify(b.result||{})]);
    return r.rowCount?json({ok:true}):json({error:"Command not found"},404);
  }catch{return json({error:"Command update failed"},500)}
}