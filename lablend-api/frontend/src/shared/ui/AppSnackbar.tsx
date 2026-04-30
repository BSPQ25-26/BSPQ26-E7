import { Alert, Snackbar } from '@mui/material'

interface AppSnackbarProps {
  open: boolean
  message: string
  severity: 'success' | 'error' | 'info' | 'warning'
  onClose: () => void
}

export const AppSnackbar = ({ open, message, severity, onClose }: AppSnackbarProps) => {
  return (
    <Snackbar open={open} autoHideDuration={3500} onClose={onClose} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
      <Alert onClose={onClose} severity={severity} variant="filled" sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  )
}
