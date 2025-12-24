export interface User {
  id: number;
  email: string;
  name: string;
  imageUrl: string | null;
  provider: string;
  roles: string[];
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}
