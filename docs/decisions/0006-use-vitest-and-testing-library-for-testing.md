---
status: "accepted"
date: 2026-04-04
decision: "Vitest + Testing Libraryをテストフレームワークとして採用する"
superseded-by: ""
---

# Vitest + Testing Library をテストフレームワークとして採用する

## Context and Problem Statement

React アプリケーションのテスト基盤を構築する必要がある。
ユニットテスト・コンポーネントテストを効率的に実行できる構成を求めている。

## Decision Drivers

- Vite との統合がネイティブであること
- テストの実行速度が速いこと
- Reactコンポーネントのテストが書きやすいこと
- ユーザー視点のテストが書けること

## Considered Options

1. Vitest + Testing Library
2. Jest + Testing Library
3. テストなし（後で導入）

## Decision Outcome

Chosen option: "Vitest + Testing Library", because Vitest は Vite の設定（エイリアス、プラグイン等）をそのまま共有でき、Testing Library はユーザー視点のテストを促進する業界標準のライブラリであるため。

### Confirmation

- `pnpm test` で全テストがパスすること
- `pnpm test:watch` でウォッチモードが機能すること

## Pros and Cons of the Options

### Vitest + Testing Library

- Good, because `vite.config.ts` の `test` ブロックに設定を統合でき、別途設定ファイルが不要
- Good, because Vite のエイリアス（`@/*`）やプラグインがテストでもそのまま動作する
- Good, because Testing Library はDOM要素をユーザーの操作に近い方法でクエリするため、実装の詳細に依存しないテストが書ける
- Good, because HMR対応のウォッチモードにより変更したファイルのテストだけが再実行される
- Neutral, because jsdom 環境はブラウザの完全な再現ではない

### Jest + Testing Library

- Good, because 最も広く使われており情報が豊富
- Bad, because ESM対応が不完全で、Viteプロジェクトとの設定の二重管理が必要
- Bad, because Viteのエイリアスやプラグインを別途Jestに設定し直す必要がある

### テストなし

- Good, because 初期セットアップの作業が不要
- Bad, because 後から導入する際に既存コードのテストを書く負担が大きくなる
- Bad, because リファクタリングや機能追加時にリグレッションを検出できない

## Consequences

- Good, `vite.config.ts` に設定が集約され構成がシンプル
- Good, `@testing-library/jest-dom/vitest` により `toBeInTheDocument()` 等のカスタムマッチャーが利用可能
- Bad, E2Eテストが必要な場合は別途 Playwright 等の導入が必要
