/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        lumen: {
          teal: '#00897B',
          tealDark: '#004D40',
          tealAccent: '#26A69A',
          indigo: '#4338CA',
          indigoDeep: '#1E1B4B',
          amber: '#F59E0B',
          amberBright: '#FBBF24',
          slateBgLight: '#F8FAFC',
          slateBgDark: '#0F172A',
          slateSurfaceDark: '#1E293B',
        },
      },
      fontFamily: {
        sans: [
          '"Segoe UI Variable"',
          '"Segoe UI"',
          'Inter',
          'system-ui',
          '-apple-system',
          'sans-serif',
        ],
        mono: [
          '"Cascadia Code"',
          '"JetBrains Mono"',
          'Consolas',
          'monospace',
        ],
      },
    },
  },
  plugins: [],
};
