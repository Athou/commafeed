export default {
    "src/**/*.{js,jsx,ts,tsx}": () => ["npm run i18n:extract", "git diff --exit-code commafeed-client/src/locales/en/messages.po"],
}
