import type { Metadata } from "next";
import "./globals.css";
export const metadata: Metadata = {
  title: "Track Car Fleet Console",
  description: "Fleet device enrollment, health and live location management."
};
export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}