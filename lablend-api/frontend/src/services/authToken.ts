const AUTH_TOKEN_KEY = 'lablend_auth_token'

export const authToken = {
  get(): string | null {
    const token = localStorage.getItem(AUTH_TOKEN_KEY)
    if (!token || token.trim().length === 0) {
      return null
    }
    return token
  },

  set(token: string): void {
    localStorage.setItem(AUTH_TOKEN_KEY, token)
  },

  clear(): void {
    localStorage.removeItem(AUTH_TOKEN_KEY)
  },
}
