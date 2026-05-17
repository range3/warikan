---
status: accepted
created_at: 2026-05-18T00:31:05+09:00
summary: "アプリ状態を URL fragment と localStorage の二段構えで永続化し、優先順位は URL > localStorage > デフォルト"
---

# アプリ状態を URL fragment と localStorage の二段構えで永続化する

## Context and Problem Statement

割り勘計算アプリは GitHub Pages 上の静的配信で、サーバサイドストレージを持たない。一方で「飲み会会場で入力した内容を後から再現したい」「他参加者に計算結果を共有したい」という 2 種類のニーズがある。前者は端末ローカルでの保持、後者は URL 共有で実現する必要がある。両者をどのように使い分け、起動時にどちらを優先するかを決める必要がある。

## Decision Drivers

* サーバを持たない（GitHub Pages 静的配信）
* 他人に計算結果を共有できる手段が必要（リンクで完結したい）
* 同一端末で前回の入力を復元したい
* カスタムプリセットは端末固有の資産であり、URL に乗せたくない
* 起動経路（URL クリック / 直接アクセス / ブックマーク）で挙動が破綻しないこと

## Considered Options

* URL fragment のみ（`#state=...`）
* localStorage のみ
* URL fragment + localStorage の二段構え
* URL query string + localStorage（`?state=...`）

## Decision Outcome

選択: **URL fragment + localStorage の二段構え**。

URL fragment は共有用（リンクを開けば即座にその状態が復元される）、localStorage は端末復元用（前回入力の自動復帰）として役割を分ける。両者が存在する場合の優先順位は **URL fragment > localStorage > デフォルトプリセット** とする。URL に乗せる内容は `bill`（合計金額・グループ構成）のみで、`presets`（カスタムプリセット）は端末固有なので localStorage のみに保持する。

fragment（`#`）を採用し query（`?`）を避ける理由:
- fragment はサーバに送信されないため GitHub Pages のアクセスログを汚さない
- SPA で URL を頻繁に書き換える時にブラウザ履歴を肥大化させない（`history.replaceState` と組み合わせる前提）
- query は SEO クローラに拾われる可能性があるが、共有用 URL に重複インデックスは不要

### Consequences

* Good — サーバ不要のまま「共有」と「復元」の両方を満たせる
* Good — URL クリック時は常に「リンクが表す状態」が表示される直感的な挙動になる
* Good — カスタムプリセットを URL に含めないため、共有 URL が肥大化しない
* Bad — URL 長制限（実用上 2KB 程度）があるため、グループ数が極端に多いと URL に乗らない可能性（割り勘の現実的な規模では問題なしと判断）
* Bad — エンコード/デコード処理を自前で書く必要がある（URL-safe Base64 + JSON、または LZString）
* Bad — `bill` のスキーマ変更時に旧 URL の互換性を考慮しないと壊れる（後方互換性は不要との CLAUDE.md 方針に従い、壊れる前提で運用）

## More Information

- 実装予定: `src/lib/url-state.ts`（fragment エンコード/デコード）+ localStorage アクセスラッパー
- データモデル詳細: `docs/plans/0001-plan.md` 第 4 章
- デプロイ環境: GitHub Pages（ADR-0003 のスタック上で動作）
- 後方互換性は維持しない（CLAUDE.md「後方互換性は必要ない」原則）
- 起動時フロー: URL fragment があれば優先採用、なければ localStorage を読み、なければデフォルトプリセット 4 種（上司/社会人/社会人学生/学生）を人数 0 で表示
