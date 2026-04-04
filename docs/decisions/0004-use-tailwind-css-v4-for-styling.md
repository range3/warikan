---
status: "accepted"
date: 2026-04-04
decision: "Tailwind CSS v4をスタイリングに採用する"
superseded-by: ""
---

# Tailwind CSS v4 をスタイリングに採用する

## Context and Problem Statement

React アプリケーションのスタイリング手法を決定する必要がある。
開発速度・パフォーマンス・保守性のバランスが取れたアプローチを求めている。

## Decision Drivers

- 高速なプロトタイピングが可能であること
- ビルドサイズが小さいこと
- 設定ファイルの少なさ（構成のシンプルさ）
- Vite との統合が容易であること

## Considered Options

1. Tailwind CSS v4
2. CSS Modules
3. vanilla CSS

## Decision Outcome

Chosen option: "Tailwind CSS v4", because `@tailwindcss/vite` プラグインによりPostCSS設定や`tailwind.config.js`が不要となり、最小限の構成でユーティリティファーストなスタイリングを実現できるため。

### Confirmation

- `@import "tailwindcss";` のみの `index.css` でTailwindクラスが機能すること
- `pnpm build` でCSS が正しくバンドルされること

## Pros and Cons of the Options

### Tailwind CSS v4

- Good, because `@tailwindcss/vite` プラグインで設定ファイル不要（PostCSS設定・tailwind.config.js が不要）
- Good, because ユーティリティクラスにより高速なUI開発が可能
- Good, because 未使用CSSが自動で除外されビルドサイズが最小
- Neutral, because クラス名が長くなりがちだがJSX内で完結する
- Bad, because カスタムデザインシステムの構築にはCSS変数のカスタマイズが必要

### CSS Modules

- Good, because 追加依存なしでコンポーネントスコープのCSSを実現
- Bad, because CSSファイルとコンポーネントファイルの往復が発生し開発速度が低下
- Bad, because ユーティリティクラスのような再利用性がない

### vanilla CSS

- Good, because 依存ゼロで最もシンプル
- Bad, because スコープ管理が困難でスタイルの衝突リスクがある
- Bad, because 大規模になるとCSS設計手法（BEM等）が別途必要

## Consequences

- Good, CSSファイルは `@import "tailwindcss";` の1行のみで構成がシンプル
- Good, Viteプラグインとして統合されておりHMRが高速
- Bad, Tailwind固有のクラス名を学習する必要がある
