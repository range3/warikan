---
status: accepted
created_at: 2026-05-18T00:31:05+09:00
summary: "ui-ux-pro-max で生成した MASTER.md + pages オーバーライド構成をデザインシステムの Source of Truth として運用する"
---

# ui-ux-pro-max で生成した MASTER.md + pages 構成でデザインシステムを運用する

## Context and Problem Statement

割り勘計算アプリの UI を実装するにあたり、配色・タイポ・コンポーネント仕様・アクセシビリティ規約などの判断を一貫させる基盤が必要になった。プロジェクト規模はミニマル（単一画面・GitHub Pages 静的配信）だが、ダークモード first-class、数値表示の精度感（`tabular-nums`）、飲み会会場での暗所視認性など、明文化しないとブレやすい要件が複数ある。デザインシステムをどこに、どの粒度で記録し、画面実装時にどう参照させるかを決める必要がある。

## Decision Drivers

* 単一画面アプリ規模に対して過剰な設計コストを払いたくない
* ダークモード/タッチ操作/数値レイアウトなど横断ルールは一箇所に集約したい
* 画面固有のレイアウト判断（4 カード並列の結果表示など）は本体ルールから分離して扱いたい
* 将来的に画面が増えた場合に追従可能な構造にしたい

## Considered Options

* インラインで都度判断（デザインシステム文書なし、コードと暗黙知に委ねる）
* CLAUDE.md にデザイン規約を全部書く
* ui-ux-pro-max スキルで初期生成 → MASTER.md として人手カスタマイズ + 画面別 `pages/<name>.md` オーバーライド
* Storybook + 専用デザイントークン管理ツール（Style Dictionary 等）を導入

## Decision Outcome

選択: **ui-ux-pro-max スキルで初期生成 → `design-system/warikan/MASTER.md` を人手カスタマイズ + `pages/<name>.md` オーバーライド**。

ui-ux-pro-max が割り勘文脈に対して生成した雛形（配色、タイポ、コンポーネント仕様、アンチパターン）はそのままでは LP 向けに寄っていたため、本プロジェクト要件（計算アプリ、ダークモード first-class、Vite + React 19 + Tailwind v4）に沿って書き換えた。本体ルールは MASTER.md に集約し、画面固有の判断は `pages/<name>.md` で差分管理する構造を採用する（同スキルが想定する Master + Overrides パターン）。CLAUDE.md は短く保ち、デザイン詳細は MASTER.md を参照させる。

### Consequences

* Good — 配色・タイポ・コンポーネント仕様が一箇所に集約され、実装時の判断ブレを抑えられる
* Good — 画面固有ルールを `pages/` で分離するため、画面追加時に MASTER を肥大化させずに済む
* Good — モック（`design-system/warikan/mockup.html`）でデザインシステムの妥当性を実装前に検証できる
* Bad — 単一画面の現状では `pages/` の構造がややオーバースペック（運用上の負担はある）
* Bad — ui-ux-pro-max の生成物は LP 寄りで、毎回手直しが前提（再生成のコストは低くない）
* Neutral — MASTER.md とコード（Tailwind 設定）の整合は人手で維持する必要がある

## More Information

- Source of Truth: `design-system/warikan/MASTER.md`
- 画面オーバーライド: `design-system/warikan/pages/<name>.md`（現時点では空）
- 視覚検証用モック: `design-system/warikan/mockup.html`（単一 HTML、Tailwind v4 CDN、ライト/ダーク両モード確認済み）
- 関連プラン: `docs/plans/0001-plan.md`
- 既存スタック決定: ADR-0003 (Vite + React + TypeScript), ADR-0004 (Tailwind CSS v4), ADR-0005 (React Compiler)
- ツールは `.claude/skills/ui-ux-pro-max/scripts/search.py --design-system --persist` で生成、人手で本体を書き換える運用とする
