/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Màu chủ đạo: Xanh dương (Giống Facebook/Messenger)
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          500: '#3b82f6',
          600: '#2563eb', // Màu chính dùng cho nút, active menu
          700: '#1d4ed8',
        },
        // Màu nền admin
        surface: '#F9FAFB', // Xám rất nhạt
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      }
    },
  },
  plugins: [],
}