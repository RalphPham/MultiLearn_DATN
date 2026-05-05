import { createI18n } from 'vue-i18n'
import en from '@/locales/en.json'
import vi from '@/locales/vi.json'

const i18n = createI18n({
  legacy: false, // Dùng cho Vue 3 Composition API
  locale: localStorage.getItem('lang') || 'vi', // Mặc định là Tiếng Việt
  fallbackLocale: 'en',
  globalInjection: true,
  messages: {
    en,
    vi
  }
})

export default i18n