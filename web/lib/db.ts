import { Pool } from "pg";
declare global { var __trackCarPool: Pool | undefined; }
export function db() {
  if (!process.env.DATABASE_URL) throw new Error("DATABASE_URL is not configured");
  return global.__trackCarPool ?? (global.__trackCarPool = new Pool({
    connectionString: process.env.DATABASE_URL, max: 5,
    ssl: { rejectUnauthorized: false }, connectionTimeoutMillis: 8000
  }));
}