import { createContext, useContext, useMemo, useState } from 'react'
import type { PropsWithChildren } from 'react'
import { authService } from '../../../services/authService'
import { parseJwtClaims, isExpired } from '../../../shared/auth/jwtClaims'
import type { UserRole } from '../../../shared/types/domain'

export interface AuthSession {
  token: string
  username: string
  role: UserRole
  userId: number
}

interface LoginInput {
  username: string
  password: string
}

interface AuthContextValue {
  isReady: boolean
  session: AuthSession | null
  login: (input: LoginInput) => Promise<AuthSession>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const buildSessionFromToken = (token: string): AuthSession | null => {
  const claims = parseJwtClaims(token)
  if (!claims || isExpired(claims) || !claims.role || !claims.userId || !claims.sub) {
    return null
  }

  return {
    token,
    role: claims.role,
    userId: claims.userId,
    username: claims.sub,
  }
}

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [session, setSession] = useState<AuthSession | null>(() => {
    const token = authService.getToken()
    if (!token) {
      return null
    }

    const nextSession = buildSessionFromToken(token)
    if (!nextSession) {
      authService.logout()
      return null
    }

    return nextSession
  })

  const value = useMemo<AuthContextValue>(
    () => ({
      isReady: true,
      session,
      async login(input: LoginInput): Promise<AuthSession> {
        const token = await authService.login({
          username: input.username,
          password: input.password,
        })

        const nextSession = buildSessionFromToken(token)
        if (!nextSession) {
          authService.logout()
          throw new Error('Login succeeded but the token does not contain role and user identity claims.')
        }

        setSession(nextSession)
        return nextSession
      },
      logout() {
        authService.logout()
        setSession(null)
      },
    }),
    [session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider.')
  }
  return context
}
