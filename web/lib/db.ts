import { Pool } from "pg";
declare global { var __trackCarPool: Pool | undefined; }
export function db() {
  const connectionString = process.env.TRACKCAR_DATABASE_URL || process.env.DATABASE_URL;
  if (!connectionString) throw new Error("TRACKCAR_DATABASE_URL or DATABASE_URL is not configured");
  return global.__trackCarPool ?? (global.__trackCarPool = new Pool({
    connectionString, max: 5,
    ssl: { rejectUnauthorized: false }, connectionTimeoutMillis: 8000
  }));
}