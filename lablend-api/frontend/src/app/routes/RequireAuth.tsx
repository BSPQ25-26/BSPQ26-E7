import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { CircularProgress, Stack } from '@mui/material'
import { useAuth } from '../../features/auth/context/AuthContext'

export const RequireAuth = () => {
  const { isReady, session } = useAuth()
  const location = useLocation()

  if (!isReady) {
    return (
      <Stack alignItems="center" justifyContent="center" minHeight="100vh">
        <CircularProgress />
      </Stack>
    )
  }

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
