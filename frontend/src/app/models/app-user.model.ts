/** Usuario de la app (fechas como string ISO, no Date). */
export interface AppUser {
  uid: string;
  email: string | null;
  premium: boolean;
  createdAt: string;
  lastSeenAt: string;
}
