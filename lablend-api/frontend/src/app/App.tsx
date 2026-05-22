import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'
import { AuthProvider } from '../features/auth/context/AuthContext'
import { AppRouter } from './routes/AppRouter'
import { appTheme } from './theme'
import { I18nProvider } from '../shared/i18n/I18nContext'

function App() {
  return (
    <ThemeProvider theme={appTheme}>
      <CssBaseline />
      <I18nProvider>
        <AuthProvider>
          <BrowserRouter>
            <AppRouter />
          </BrowserRouter>
        </AuthProvider>
      </I18nProvider>
    </ThemeProvider>
  )
}

export default App
