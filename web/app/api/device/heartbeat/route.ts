import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { deviceFromRequest, json } from "@/lib/security";
export const runtime = "nodejs";
export async function POST(req:NextRequest) {
  const device=await deviceFromRequest(req); if(!device)return json({error:"Unauthorized device"},401);
  let b:any={};try{b=await req.json()}catch{return json({error:"Invalid JSON"},400)}
  const battery=Number.isFinite(Number(b.battery_percent))?Math.max(0,Math.min(100,Number(b.battery_percent))):null;
  try{
    await db().query("UPDATE devices SET status='online',last_seen_at=now(),battery_percent=$2,agent_version=COALESCE($3,agent_version),updated_at=now() WHERE id=$1",
      [device.id,battery,b.agent_version?String(b.agent_version).slice(0,40):null]);
    return json({ok:true,server_time:new Date().toISOString()});
  }catch{return json({error:"Heartbeat failed"},500)}
}