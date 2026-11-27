import { db } from "@/lib/db";
import { json } from "@/lib/security";
export const runtime = "nodejs";
export async function GET() {
  try { await db().query("SELECT 1"); return json({ok:true,service:"track-car-api",database:"connected",time:new Date().toISOString()}); }
  catch { return json({ok:false,service:"track-car-api",database:"unavailable"},503); }
}