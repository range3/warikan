---
status: "accepted"
date: 2026-04-04
decision: "Vite + React + TypeScriptをフロントエンドスタックとして採用する"
superseded-by: ""
---

# Vite + React + TypeScript をフロントエンドスタックとして採用する

## Context and Problem Statement

割り勘アプリのフロントエンドを構築するにあたり、ビルドツール・UIライブラリ・言語の組み合わせを決定する必要がある。
モダンで高速な開発体験と型安全性を両立した構成を求めている。

## Decision Drivers

- 高速なHMR（Hot Module Replacement）による開発体験
- 型安全性によるバグの早期発見
- エコシステムの成熟度とコミュニティの活発さ
- SPAとして十分な要件（SSR不要）
- 既存のBiome設定との親和性

## Considered Options

1. Vite + React + TypeScript
2. Next.js（App Router）
3. Remix
4. Vite + Vue + TypeScript

## Decision Outcome

Chosen option: "Vite + React + TypeScript", because SPAで十分な要件に対してNext.jsやRemixのSSR機能はオーバースペックであり、Vite 8（Rolldownベース）の高速なビルドとReactの成熟したエコシステムが最適なため。

### Confirmation

- `pnpm build` でプロダクションビルドが成功すること
- `pnpm dev` で開発サーバーが起動しHMRが機能すること

## Pros and Cons of the Options

### Vite + React + TypeScript

- Good, because Vite 8はRolldownベースで非常に高速なビルドを実現
- Good, because Reactは最も広く使われるUIライブラリであり、エコシステムが充実
- Good, because SPAに特化しておりシンプルな構成を維持できる
- Good, because Biome 2.xがJSX/TSXをネイティブサポート
- Neutral, because SSRが必要になった場合は追加設定が必要

### Next.js（App Router）

- Good, because SSR/SSG/ISRなど柔軟なレンダリング戦略を選択可能
- Bad, because SPAのみの要件に対してフレームワークの複雑さがオーバースペック
- Bad, because 独自のビルドシステム（Turbopack）であり、Viteエコシステムと互換性がない

### Remix

- Good, because Web標準に準拠した設計
- Bad, because SPAモードはあるがViteほどシンプルではない
- Bad, because Reactエコシステムとの統合がNext.jsほど成熟していない

### Vite + Vue + TypeScript

- Good, because Viteとの親和性が高い（同じ作者）
- Bad, because Reactと比較してエコシステムが小さく、ライブラリの選択肢が少ない

## Consequences

- Good, 高速な開発サーバーとビルドにより開発体験が向上する
- Good, TypeScriptによる型チェック（`tsc -b`）でビルド時にエラーを検出できる
- Bad, SSRが必要になった場合は構成の見直しが必要になる

## More Information

- Vite 8はRolldownバンドラーを採用し、esbuild/Rollup依存から脱却
- TypeScript 5.9の`erasableSyntaxOnly`オプションを有効化し、型注釈の実行時残留を防止
- tsconfig はプロジェクト参照（`tsconfig.app.json` + `tsconfig.node.json`）で分離
