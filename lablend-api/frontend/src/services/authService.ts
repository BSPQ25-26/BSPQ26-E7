import { httpClient } from './http/client'
import { authToken } from './authToken'

interface LoginPayload {
  username?: string
  email?: string
  password: string
}

type LoginResponse = {
  token?: string
  accessToken?: string
  jwt?: string
  jwtToken?: string
}

const extractToken = (response: LoginResponse): string => {
  const tokenCandidate = response.accessToken ?? response.token ?? response.jwt ?? response.jwtToken
  if (!tokenCandidate || tokenCandidate.trim().length === 0) {
    throw new Error('Login succeeded but no token was returned by the server.')
  }
  return tokenCandidate
}

export const authService = {
  async login(payload: LoginPayload): Promise<string> {
    const response = await httpClient.post<LoginResponse, LoginPayload>('/auth/login', payload)
    const token = extractToken(response)
    authToken.set(token)
    return token
  },

  logout(): void {
    authToken.clear()
  },

  getToken(): string | null {
    return authToken.get()
  },
}

export type { LoginPayload }
