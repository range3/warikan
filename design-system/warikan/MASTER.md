# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/warikan/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** Warikan (傾斜配分対応の割り勘計算アプリ)
**Stack:** Vite + React 19 + TypeScript + Tailwind CSS v4
**Deploy:** GitHub Pages (静的)
**Target:** スマホ会場入力ファースト / ダークモード first-class / 1画面完結

---

## Design Principles

1. **入力の速さ** — 飲み会会場で片手・数十秒で完了。タップ数を最小化する。
2. **数字の信頼感** — 等幅数字 (`tabular-nums`) と十分なコントラストで誤読を防ぐ。
3. **暗所での視認性** — ダークモードを first-class でサポート（partial 対応は不可）。
4. **シンプルな視覚** — Flat Design。装飾的なシャドウ・グラデは使わない。border と色面で構造を作る。
5. **可逆性** — 破壊的操作 (削除/リセット) は必ず確認 or Undo を提供。

---

## Color Tokens

CSS 変数で定義し、light/dark を `prefers-color-scheme` で切替える。Tailwind v4 の `@theme` ディレクティブから参照する。

### Semantic Roles

| Role | Light | Dark | Usage |
|------|-------|------|-------|
| `--color-bg` | `#FFFFFF` | `#0A0A0A` | アプリ全体の背景 |
| `--color-surface` | `#F8FAFC` | `#18181B` | カード / 入力エリア |
| `--color-surface-elevated` | `#FFFFFF` | `#27272A` | モーダル / トースト |
| `--color-fg` | `#0F172A` | `#FAFAFA` | 主要テキスト |
| `--color-fg-muted` | `#475569` | `#A1A1AA` | 補助テキスト・ラベル |
| `--color-border` | `#E2E8F0` | `#3F3F46` | 区切り線・入力枠 |
| `--color-primary` | `#059669` | `#10B981` | 主要 CTA・正の数値 (集金額) |
| `--color-on-primary` | `#FFFFFF` | `#0A0A0A` | Primary 上のテキスト |
| `--color-destructive` | `#DC2626` | `#EF4444` | 削除・リセット・損益マイナス |
| `--color-warning` | `#D97706` | `#F59E0B` | 警告 (端数大きい等) |
| `--color-ring` | `#059669` | `#10B981` | フォーカスリング |

**色の使い分け原則**
- **緑 = 正/集金/確定** (主要 CTA、集金額、幹事プラス)
- **赤 = 削除/警告/マイナス** (リセット、グループ削除、幹事マイナス)
- 色だけで意味を伝えない。アイコン/記号 (+/−) を必ず併用。

---

## Tailwind v4 Theme Setup

`src/index.css` で以下のように設定する想定:

```css
@import "tailwindcss";

@theme {
  --color-bg: #FFFFFF;
  --color-surface: #F8FAFC;
  --color-surface-elevated: #FFFFFF;
  --color-fg: #0F172A;
  --color-fg-muted: #475569;
  --color-border: #E2E8F0;
  --color-primary: #059669;
  --color-on-primary: #FFFFFF;
  --color-destructive: #DC2626;
  --color-warning: #D97706;
  --color-ring: #059669;

  --font-sans: "Inter", ui-sans-serif, system-ui, sans-serif;
  --font-mono: "Inter", ui-monospace, monospace; /* Inter は tabular-nums をサポート */

  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;

  --shadow-focus: 0 0 0 3px color-mix(in oklch, var(--color-primary) 35%, transparent);
}

@layer base {
  @media (prefers-color-scheme: dark) {
    @theme {
      --color-bg: #0A0A0A;
      --color-surface: #18181B;
      --color-surface-elevated: #27272A;
      --color-fg: #FAFAFA;
      --color-fg-muted: #A1A1AA;
      --color-border: #3F3F46;
      --color-primary: #10B981;
      --color-on-primary: #0A0A0A;
      --color-destructive: #EF4444;
      --color-warning: #F59E0B;
      --color-ring: #10B981;
    }
  }

  html, body { background: var(--color-bg); color: var(--color-fg); }
  body { font-family: var(--font-sans); }
}
```

---

## Typography

- **Family:** Inter (heading + body 共通)
- **Numeric:** すべての金額・人数・weight 表示は `font-variant-numeric: tabular-nums` (Tailwind: `tabular-nums`)
- **Sizes (mobile-first)**
  - `text-xs` 12px — 補助ラベル
  - `text-sm` 14px — フォームラベル
  - `text-base` 16px — 本文・入力 (iOS 自動ズーム回避のため最低 16px)
  - `text-lg` 18px — 強調ラベル
  - `text-2xl` 24px — 結果カードの 1人あたり金額
  - `text-3xl` 30px — 合計金額入力
  - `text-4xl` 36px — 結果サマリの主要数値

**Google Fonts CDN は使わず、`@fontsource-variable/inter` を npm 経由で同梱**（GitHub Pages のオフラインでも安定、CLS 防止）。

---

## Spacing & Layout

| Token | Value | Usage |
|-------|-------|-------|
| `--space-xs` | `4px` | アイコンと文字の間 |
| `--space-sm` | `8px` | インライン要素間 |
| `--space-md` | `16px` | カード内 padding / 入力間 |
| `--space-lg` | `24px` | セクション間 |
| `--space-xl` | `32px` | 主要セクション境界 |

- **コンテナ最大幅**: `max-w-md` (28rem / 448px) を mobile 〜 desktop で維持。PC でも中央 1 カラムで使う。
- **safe-area**: `env(safe-area-inset-bottom)` を下部固定 Action Bar に必ず適用。

---

## Elevation

Flat Design に従い、**カードに shadow は使わない**。区別は `border` + `background` で行う。

| 用途 | 方法 |
|------|------|
| カード / グループ | `bg-surface border border-border rounded-lg` |
| 結果カード (強調) | `bg-surface border-2 border-primary rounded-lg` |
| モーダル / トースト | `bg-surface-elevated border border-border rounded-xl shadow-lg`（モーダルのみ shadow 許容） |
| Focus | `outline-none ring-2 ring-ring ring-offset-2 ring-offset-bg` |

---

## Page Pattern: Input → Result Calculator

LP パターンではなく、**入力 → 結果が同一画面で見える 1 カラム実用パターン**。

```
┌─────────────────────────────┐
│ ヘッダー (タイトルのみ、最小)    │
├─────────────────────────────┤
│ [合計金額] 大きな数値入力       │
├─────────────────────────────┤
│ [グループ一覧]                  │
│  ├ グループカード × N          │
│  └ [+ グループ追加]             │
├─────────────────────────────┤
│ [結果: 4カード横スクロール]     │
│   1円 / 100 / 500 / 1000     │
├─────────────────────────────┤
│ (スクロール余白)                │
├─────────────────────────────┤
│ [固定下部 Action Bar]           │
│  [URLコピー] [リセット (赤)]    │
└─────────────────────────────┘
```

**ルール**
- ヘッダーは sticky にしない（縦領域を稼ぐため）
- 下部 Action Bar のみ sticky + safe-area
- 入力 → 結果は同一スクロール上で見える。結果セクションは scroll-margin で「結果へ」リンクから飛べるようにしてもよい

---

## Component Specs

### Number Input (金額・人数)

```tsx
<input
  type="text"
  inputMode="numeric"   // iOS で数字キーパッド
  pattern="[0-9]*"
  className="
    w-full px-4 py-3 text-2xl tabular-nums
    bg-surface text-fg
    border border-border rounded-md
    focus:outline-none focus:ring-2 focus:ring-ring
    min-h-[48px]
  "
/>
```

- `type="number"` は使わない（iOS のスピナー UX が悪く、`0` 始まり問題もあるため）
- 表示はカンマ区切り (`Intl.NumberFormat('ja-JP')`)、保存は数値
- 最小タップ領域 48×48px (Material) を満たすため `min-h-[48px]`

### Weight Stepper (0.1 刻み)

```tsx
<div className="inline-flex items-center gap-2">
  <button className="w-11 h-11 rounded-md border border-border active:scale-95" aria-label="weight を 0.1 減らす">−</button>
  <span className="min-w-[3ch] text-center text-lg tabular-nums">1.0</span>
  <button className="w-11 h-11 rounded-md border border-border active:scale-95" aria-label="weight を 0.1 増やす">+</button>
</div>
```

- 上限なし、下限 0.1
- 長押し連打対応 (PointerEvent + setInterval) は将来拡張
- `active:scale-95` で押下フィードバック（150ms）

### Mode Toggle (weight / fixed)

```tsx
<div role="tablist" className="inline-flex p-1 bg-muted rounded-md">
  <button role="tab" aria-selected={mode === 'weight'}
    className="px-3 py-1.5 rounded-sm text-sm aria-selected:bg-surface-elevated aria-selected:shadow-sm">
    傾斜
  </button>
  <button role="tab" aria-selected={mode === 'fixed'}
    className="px-3 py-1.5 rounded-sm text-sm aria-selected:bg-surface-elevated aria-selected:shadow-sm">
    固定額
  </button>
</div>
```

### Group Card

```tsx
<section className="bg-surface border border-border rounded-lg p-4 space-y-3">
  <div className="flex items-center justify-between gap-2">
    <input className="text-lg font-semibold bg-transparent" placeholder="グループ名" />
    <button aria-label="グループを削除" className="text-destructive">{/* trash icon */}</button>
  </div>
  {/* 人数 / モード切替 / weight or 固定額 / 預り金 (collapsible) */}
</section>
```

### Result Card (4 並列)

```tsx
<div className="overflow-x-auto -mx-4 px-4 snap-x snap-mandatory">
  <div className="flex gap-3">
    {[1, 100, 500, 1000].map(unit => (
      <article key={unit} className="snap-start shrink-0 w-[280px] bg-surface border-2 border-border rounded-lg p-4 space-y-2">
        <h3 className="text-sm text-fg-muted">{unit}円単位</h3>
        <dl className="space-y-1 text-base tabular-nums">
          {/* グループ別 1 人あたり */}
        </dl>
        <div className="pt-2 border-t border-border text-sm tabular-nums">
          幹事差額:
          <span className={diff >= 0 ? 'text-primary' : 'text-destructive'}>
            {diff >= 0 ? '+' : ''}{diff.toLocaleString('ja-JP')}円
          </span>
        </div>
      </article>
    ))}
  </div>
</div>
```

- 横スクロール + `snap-x` でスマホで 1 枚ずつスナップ
- desktop (md 以上) は `flex-wrap` で 2×2 グリッドへ切替可

### Bottom Action Bar (固定)

```tsx
<div className="
  fixed bottom-0 inset-x-0
  bg-surface-elevated border-t border-border
  px-4 pt-3
  pb-[calc(0.75rem+env(safe-area-inset-bottom))]
  flex gap-3
">
  <button className="flex-1 h-12 bg-primary text-on-primary rounded-md font-semibold active:scale-[0.98]">
    URLをコピー
  </button>
  <button className="h-12 px-4 border border-destructive text-destructive rounded-md active:scale-[0.98]">
    リセット
  </button>
</div>
```

- 主要 CTA = 緑塗り、破壊的 = 赤 outline（誤タップ抑止のため塗りつぶしを避ける）
- 本体スクロール領域には `pb-24` 程度の余白を入れて Action Bar に隠れないようにする

### Toast (URL コピー完了等)

- 3 秒で自動 dismiss
- `aria-live="polite"` で SR 通知
- 画面上部または Action Bar 上に表示、Bar には被せない

---

## Interaction & Motion

- **遷移時間**: 150-250ms、ease-out
- **タップ feedback**: `active:scale-[0.97]` または `active:scale-95`、`transform` のみで layout-shift なし
- **prefers-reduced-motion**: アニメは disable、即時切替に
- **入力 → 結果反映**: 即時 (debounce 不要、計算は軽量)

---

## State Persistence

- **localStorage**: 最後の入力状態 + カスタムプリセット
- **URL**: 現在状態を URL-safe Base64 (or LZString) で fragment (`#state=...`) にエンコード。`?` でなく `#` を使う理由は GitHub Pages の SSR を介さず履歴を汚さないため
- 起動時の優先順位: `URL fragment` > `localStorage` > デフォルトプリセット

---

## Anti-Patterns (Do NOT Use)

- ❌ **絵文字をアイコンとして使用** → Lucide-react を採用 (`pnpm add lucide-react`)
- ❌ **複雑な shadow / グラデ** → border + 色面で構造化
- ❌ **`type="number"` の input** → iOS UX 不良
- ❌ **色だけで意味を伝える** → ±記号 / アイコンを必ず併用
- ❌ **placeholder のみのラベル** → 必ず可視ラベルを併設
- ❌ **layout-shift する hover/press** → `transform` のみ使用
- ❌ **CDN フォント** → `@fontsource-variable/inter` で同梱
- ❌ **`shadow-md` を card に常用** → flat 維持
- ❌ **モーダルで主要フローを遮断** → 計算は常に背面で見える状態を維持

---

## Pre-Delivery Checklist

実装した画面/コンポーネントを deliver する前に確認:

### Visual
- [ ] 絵文字アイコン不使用、すべて Lucide SVG
- [ ] カードに装飾的 shadow なし (border ベース)
- [ ] light/dark 両モードでスクリーンショット確認
- [ ] 数値表示すべてに `tabular-nums`

### Interaction
- [ ] すべての操作対象が最小 44×44pt (実測 48px 推奨)
- [ ] フォーカスリングが light/dark 両方で見える
- [ ] `active:` 状態で押下 feedback あり
- [ ] `prefers-reduced-motion` でアニメ disable

### Layout
- [ ] 375px / 768px / 1024px で確認
- [ ] 下部固定 Action Bar が safe-area を考慮
- [ ] スクロール領域が Bar に隠れない
- [ ] 横スクロール (snap) が iOS Safari で動く

### Accessibility
- [ ] アイコンのみのボタンに `aria-label`
- [ ] フォームに `<label>` (placeholder のみ不可)
- [ ] 数値変更ステッパーのキーボード操作（↑↓ で増減）
- [ ] エラー時 `role="alert"` または `aria-live`
- [ ] body text コントラスト 4.5:1 以上 (両モード)

### Performance
- [ ] フォントは self-host (`@fontsource-variable/inter`)
- [ ] 計算は同期で OK (React Compiler 任せ、手動 memo 不要)
- [ ] 状態変更で layout-thrash なし
