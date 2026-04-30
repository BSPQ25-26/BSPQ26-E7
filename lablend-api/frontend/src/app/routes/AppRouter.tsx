import { Alert, Box, Button, Stack, Typography } from '@mui/material'
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/context/AuthContext'
import { LoginPage } from '../../features/auth/pages/LoginPage'
import { AdminDashboardPage } from '../../features/admin/pages/AdminDashboardPage'
import { UserDashboardPage } from '../../features/user/pages/UserDashboardPage'
import { RequireAuth } from './RequireAuth'
import { RequireRole } from './RequireRole'

const HomeRedirect = () => {
  const { session } = useAuth()
  if (!session) {
    return <Navigate to="/login" replace />
  }
  return <Navigate to={session.role === 'ADMIN' ? '/admin' : '/user'} replace />
}

const ForbiddenPage = () => {
  const navigate = useNavigate()
  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 3 }}>
      <Stack spacing={2} maxWidth={560}>
        <Alert severity="error">You do not have permission to access this route.</Alert>
        <Button variant="contained" onClick={() => navigate('/', { replace: true })}>Return to dashboard</Button>
      </Stack>
    </Box>
  )
}

const NotFoundPage = () => {
  const navigate = useNavigate()
  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 3 }}>
      <Stack spacing={2} alignItems="center">
        <Typography variant="h4">Page not found</Typography>
        <Button variant="contained" onClick={() => navigate('/', { replace: true })}>Go home</Button>
      </Stack>
    </Box>
  )
}

export const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />

      <Route element={<RequireAuth />}>
        <Route element={<RequireRole role="ADMIN" />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
        </Route>

        <Route element={<RequireRole role="USER" />}>
          <Route path="/user" element={<UserDashboardPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
