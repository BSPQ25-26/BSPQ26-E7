import type { UserRole } from '../types/domain'

export interface JwtClaims {
  sub?: string
  role?: UserRole
  userId?: number
  exp?: number
  iat?: number
}

const normalizeRole = (role: unknown): UserRole | null => {
  if (role === 'ADMIN' || role === 'USER') {
    return role
  }
  return null
}

const normalizeUserId = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
    return value
  }
  if (typeof value === 'string') {
    const parsed = Number(value)
    if (Number.isFinite(parsed) && parsed > 0) {
      return parsed
    }
  }
  return null
}

const decodeBase64Url = (input: string): string => {
  const normalized = input.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  return atob(padded)
}

export const parseJwtClaims = (token: string): JwtClaims | null => {
  const parts = token.split('.')
  if (parts.length < 2) {
    return null
  }

  try {
    const payload = JSON.parse(decodeBase64Url(parts[1])) as Record<string, unknown>
    const role = normalizeRole(payload.role)
    const userId = normalizeUserId(payload.userId)

    const claims: JwtClaims = {
      sub: typeof payload.sub === 'string' ? payload.sub : undefined,
      role: role ?? undefined,
      userId: userId ?? undefined,
      exp: typeof payload.exp === 'number' ? payload.exp : undefined,
      iat: typeof payload.iat === 'number' ? payload.iat : undefined,
    }

    return claims
  } catch {
    return null
  }
}

export const isExpired = (claims: JwtClaims): boolean => {
  if (!claims.exp) {
    return false
  }

  const nowInSeconds = Math.floor(Date.now() / 1000)
  return claims.exp <= nowInSeconds
}
