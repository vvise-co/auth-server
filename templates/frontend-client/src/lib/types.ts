export interface User {
  id: number;
  email: string;
  name: string;
  imageUrl?: string;
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

export interface ApiError {
  message: string;
  status: number;
}
