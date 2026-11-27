import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { deviceFromRequest, json } from "@/lib/security";
export const runtime = "nodejs";
export async function POST(req:NextRequest) {
  const device=await deviceFromRequest(req);if(!device)return json({error:"Unauthorized device"},401);
  let b:any={};try{b=await req.json()}catch{return json({error:"Invalid JSON"},400)}
  const input=Array.isArray(b.locations)?b.locations:[b];
  if(input.length<1||input.length>100)return json({error:"Send between 1 and 100 location samples"},400);
  const rows=[];
  for(const p of input){
    const lat=Number(p.latitude),lng=Number(p.longitude),accuracy=p.accuracy_m==null?null:Number(p.accuracy_m),speed=p.speed_kmh==null?null:Number(p.speed_kmh);
    if(!Number.isFinite(lat)||lat < -90||lat>90||!Number.isFinite(lng)||lng < -180||lng>180)continue;
    rows.push({latitude:lat,longitude:lng,accuracy_m:Number.isFinite(accuracy as number)?accuracy:null,speed_kmh:Number.isFinite(speed as number)?speed:null,recorded_at:p.recorded_at||new Date().toISOString()});
  }
  if(!rows.length)return json({error:"No valid coordinates"},400);
  const client=await db().connect();
  try{
    await client.query("BEGIN");
    await client.query(
      "INSERT INTO locations(device_id,latitude,longitude,accuracy_m,speed_kmh,recorded_at) SELECT $1,x.latitude,x.longitude,x.accuracy_m,x.speed_kmh,COALESCE(x.recorded_at,now()) FROM jsonb_to_recordset($2::jsonb) AS x(latitude double precision,longitude double precision,accuracy_m double precision,speed_kmh double precision,recorded_at timestamptz)",
      [device.id,JSON.stringify(rows)]
    );
    const last=rows[rows.length-1];
    await client.query("UPDATE devices SET status='online',last_seen_at=now(),last_lat=$2,last_lng=$3,updated_at=now() WHERE id=$1",[device.id,last.latitude,last.longitude]);
    await client.query("COMMIT");
    return json({ok:true,accepted:rows.length});
  }catch{await client.query("ROLLBACK");return json({error:"Location sync failed"},500)}
  finally{client.release()}
}