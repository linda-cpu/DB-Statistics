export interface AuthResponse {
  id: number;
  token: string;
  role: "ADMIN" | "USER";
}
