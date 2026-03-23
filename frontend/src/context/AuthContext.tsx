import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'

interface AuthContextType {
  token: string | null
  username: string | null
  isAuthenticated: boolean
  login: (token: string, username: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

const TOKEN_KEY = 'medilabo_token'
const USER_KEY = 'medilabo_user'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem(USER_KEY))

  const loginFn = (newToken: string, newUsername: string) => {
    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(USER_KEY, newUsername)
    setToken(newToken)
    setUsername(newUsername)
  }

  const logoutFn = () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUsername(null)
  }

  const value = useMemo<AuthContextType>(
    () => ({
      token,
      username,
      isAuthenticated: !!token,
      login: loginFn,
      logout: logoutFn,
    }),
    [token, username],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
