import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { PropsWithChildren } from 'react'
import { defaultLanguage, languages, translations } from './translations'
import type { Language, TranslationKey } from './translations'

type TranslationParams = Record<string, string | number>

interface I18nContextValue {
  language: Language
  setLanguage: (language: Language) => void
  t: (key: TranslationKey, params?: TranslationParams) => string
}

const STORAGE_KEY = 'lablend.language'
const I18nContext = createContext<I18nContextValue | undefined>(undefined)

const isLanguage = (value: string | null): value is Language =>
  value !== null && languages.includes(value as Language)

const readInitialLanguage = (): Language => {
  const storedLanguage = window.localStorage.getItem(STORAGE_KEY)
  if (isLanguage(storedLanguage)) {
    return storedLanguage
  }

  const browserLanguage = window.navigator.language.slice(0, 2)
  if (isLanguage(browserLanguage)) {
    return browserLanguage
  }

  return defaultLanguage
}

const interpolate = (message: string, params?: TranslationParams): string => {
  if (!params) {
    return message
  }

  return Object.entries(params).reduce(
    (nextMessage, [name, value]) => nextMessage.replaceAll(`{${name}}`, String(value)),
    message,
  )
}

export const I18nProvider = ({ children }: PropsWithChildren) => {
  const [language, setLanguageState] = useState<Language>(readInitialLanguage)

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, language)
    document.documentElement.lang = language
  }, [language])

  const value = useMemo<I18nContextValue>(
    () => ({
      language,
      setLanguage(nextLanguage: Language) {
        setLanguageState(nextLanguage)
      },
      t(key: TranslationKey, params?: TranslationParams) {
        return interpolate(translations[language][key] ?? translations[defaultLanguage][key], params)
      },
    }),
    [language],
  )

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export const useI18n = (): I18nContextValue => {
  const context = useContext(I18nContext)
  if (!context) {
    throw new Error('useI18n must be used inside an I18nProvider.')
  }
  return context
}
