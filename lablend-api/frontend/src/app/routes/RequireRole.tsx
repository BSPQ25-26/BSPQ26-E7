import { Navigate, Outlet } from 'react-router-dom'
import type { UserRole } from '../../shared/types/domain'
import { useAuth } from '../../features/auth/context/AuthContext'

interface RequireRoleProps {
  role: UserRole
}

export const RequireRole = ({ role }: RequireRoleProps) => {
  const { session } = useAuth()

  if (!session) {
    return <Navigate to="/login" replace />
  }

  if (session.role !== role) {
    const redirectPath = session.role === 'ADMIN' ? '/admin' : '/user'
    return <Navigate to={redirectPath} replace />
  }

  return <Outlet />
}
