import { createTheme } from '@mui/material/styles'

export const appTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#0f766e',
    },
    secondary: {
      main: '#2563eb',
    },
    background: {
      default: '#f5f7fb',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: 'Poppins, "Segoe UI", sans-serif',
    h3: {
      fontWeight: 700,
      letterSpacing: '-0.03em',
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 14,
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 12px 30px rgba(15, 23, 42, 0.08)',
        },
      },
    },
  },
})
