module.exports = {
    root: true,
    env: {
        es6: true,
        node: true,
    },
    extends: [
        "eslint:recommended",
        "google",
    ],
    parserOptions: {
        "parser": "@babel/eslint-parser",
        "sourceType": "module",
        "ecmaVersion": "latest",
        "ecmaFeatures": {
            "jsx": true,
            "experimentalObjectRestSpread": true,
        },
        "requireConfigFile": false,
    },
    rules: {
        "object-curly-spacing": 0,
        "quotes": 0,
        "semi": 0,
        "max-len": 0,
        "padded-blocks": 0,
        "require-jsdoc": 0,
        "operator-linebreak": 0,
        "guard-for-in": 1,
        "no-unused-vars": 1,
        "indent": ["error", 4],
    },
};
