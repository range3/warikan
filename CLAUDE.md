# プロジェクト概要

## 設計判断ルール（ADR）

このプロジェクトでは MADR 4.0.0（+ 独自フロントマター拡張）を用いて設計判断を記録する。
ADR の作成・管理は `/adr` スキルで行う。

- 技術的な作業を始める前に `/adr` で既存の設計判断を確認し、関連する ADR があればコンテキストに含めること
- 設計判断（技術選定、構成変更、パターン採用など）が行われた場合は `/adr` スキルを使って ADR を残すこと

## ブラウザ操作（playwright-cli）

プロジェクトローカルにインストール済み。グローバルインストールではなく `pnpm exec` 経由で使うこと。

```bash
pnpm exec playwright-cli open http://localhost:5173/
pnpm exec playwright-cli snapshot
pnpm exec playwright-cli close
```
