/**
 * User interface compliant with OpenID Connect Core 1.0 Standard Claims.
 * @see https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
 */
export interface User {
  // OIDC: sub - Subject Identifier
  sub: string;
  // OIDC: name - Full name
  name: string;
  // OIDC: given_name
  given_name?: string;
  // OIDC: family_name
  family_name?: string;
  // OIDC: middle_name
  middle_name?: string;
  // OIDC: nickname
  nickname?: string;
  // OIDC: preferred_username
  preferred_username?: string;
  // OIDC: profile - URL of profile page
  profile?: string;
  // OIDC: picture - URL of profile picture
  picture?: string;
  // OIDC: website
  website?: string;
  // OIDC: email
  email: string;
  // OIDC: email_verified
  email_verified?: boolean;
  // OIDC: gender
  gender?: string;
  // OIDC: birthdate (YYYY-MM-DD format)
  birthdate?: string;
  // OIDC: zoneinfo
  zoneinfo?: string;
  // OIDC: locale
  locale?: string;
  // OIDC: phone_number
  phone_number?: string;
  // OIDC: phone_number_verified
  phone_number_verified?: boolean;
  // OIDC: address
  address?: Address;
  // OIDC: updated_at (Unix timestamp)
  updated_at?: number;
  // Application-specific fields
  roles?: string[];
  provider?: string;
}

/**
 * OIDC Address Claim structure.
 */
export interface Address {
  formatted?: string;
  street_address?: string;
  locality?: string;
  region?: string;
  postal_code?: string;
  country?: string;
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
