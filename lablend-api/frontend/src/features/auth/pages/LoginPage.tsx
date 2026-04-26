import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export const LoginPage = () => {
  const navigate = useNavigate()
  const { session, login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  if (session) {
    const redirectPath = session.role === 'ADMIN' ? '/admin' : '/user'
    return <Navigate to={redirectPath} replace />
  }

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!username.trim() || !password.trim()) {
      setErrorMessage('Username and password are required.')
      return
    }

    setIsSubmitting(true)
    setErrorMessage(null)

    try {
      const authSession = await login({ username: username.trim(), password })
      navigate(authSession.role === 'ADMIN' ? '/admin' : '/user', { replace: true })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to log in.'
      setErrorMessage(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        background:
          'radial-gradient(circle at 20% 20%, rgba(15, 118, 110, 0.15), transparent 45%), radial-gradient(circle at 80% 0%, rgba(37, 99, 235, 0.12), transparent 35%)',
        px: 2,
      }}
    >
      <Card sx={{ width: '100%', maxWidth: 460 }}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={2.5} component="form" onSubmit={onSubmit}>
            <Typography variant="h4" fontWeight={700}>
              LabLend
            </Typography>
            <Typography color="text.secondary">Sign in to continue to your workspace.</Typography>

            {errorMessage ? <Alert severity="error">{errorMessage}</Alert> : null}

            <TextField
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              label="Username"
              autoComplete="username"
              fullWidth
            />
            <TextField
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              label="Password"
              type="password"
              autoComplete="current-password"
              fullWidth
            />

            <Button variant="contained" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Signing in...' : 'Sign in'}
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  )
}
