import { NextRequest } from "next/server";
import { db } from "@/lib/db";
import { adminAuthorized, json } from "@/lib/security";
export const runtime = "nodejs";
export async function GET(req:NextRequest) {
  if (!adminAuthorized(req)) return json({error:"Unauthorized"},401);
  try {
    const result=await db().query(
      "SELECT d.id,d.device_code,d.label,d.status,d.manufacturer,d.model,d.android_release,d.sdk_int,d.last_seen_at,d.last_lat,d.last_lng,v.plate_number FROM public.devices d LEFT JOIN public.vehicles v ON v.id=d.vehicle_id ORDER BY d.last_seen_at DESC NULLS LAST,d.created_at DESC LIMIT 500"
    );
    return json({devices:result.rows});
  } catch { return json({error:"Could not load devices"},500); }
}