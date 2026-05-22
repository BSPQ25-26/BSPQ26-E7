import { FormControl, InputLabel, MenuItem, Select } from '@mui/material'
import { languages } from './translations'
import type { Language } from './translations'
import { useI18n } from './I18nContext'

export const LanguageSwitcher = () => {
  const { language, setLanguage, t } = useI18n()

  return (
    <FormControl size="small" sx={{ minWidth: 150 }}>
      <InputLabel id="language-switcher-label">{t('language.label')}</InputLabel>
      <Select
        labelId="language-switcher-label"
        value={language}
        label={t('language.label')}
        onChange={(event) => setLanguage(event.target.value as Language)}
      >
        {languages.map((option) => (
          <MenuItem key={option} value={option}>
            {option === 'en' ? t('language.english') : t('language.spanish')}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  )
}
