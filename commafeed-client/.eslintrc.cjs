module.exports = {
    env: {
        browser: true,
        es2021: true
    },
    extends: ["standard-with-typescript", "plugin:react/recommended", "plugin:react-hooks/recommended", "plugin:prettier/recommended"],
    settings: {
        react: {
            version: "detect"
        }
    },
    overrides: [
        {
            env: {
                node: true
            },
            files: [".eslintrc.{js,cjs}"],
            parserOptions: {
                sourceType: "script"
            }
        }
    ],
    parserOptions: {
        ecmaVersion: "latest",
        sourceType: "module"
    },
    plugins: ["react"],
    rules: {
        "@typescript-eslint/consistent-type-assertions": ["error", { assertionStyle: "as" }],
        "@typescript-eslint/explicit-function-return-type": "off",
        "@typescript-eslint/no-confusing-void-expression": ["error", { ignoreArrowShorthand: true }],
        "@typescript-eslint/no-floating-promises": "off",
        "@typescript-eslint/no-misused-promises": "off",
        "@typescript-eslint/prefer-nullish-coalescing": ["error", { ignoreConditionalTests: true }],
        "@typescript-eslint/strict-boolean-expressions": "off",
        "@typescript-eslint/unbound-method": "off",
        "react/no-unescaped-entities": "off",
        "react/react-in-jsx-scope": "off",
        "react-hooks/exhaustive-deps": "error"
    }
}
