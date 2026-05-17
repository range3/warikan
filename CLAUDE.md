# 原則
- 後方互換性は必要ない
- ドキュメントとコードが矛盾している場合は、コード優先

# プロジェクト開始時

```bash
pnpm install
pnpm exec skills experimental_sync
```

# ビルド・主要コマンド

```bash
pnpm dev         # 開発サーバ
pnpm build       # 型チェック + ビルド
pnpm test        # テスト実行
pnpm check       # Biome チェック
pnpm check:fix   # Biome 自動修正
```

# ADR
docs/decisions/INDEX.mdを参照しADRの一覧とサマリーを確認すること
必要に応じて、ADR-NNNN-*.mdを参照する

# ブラウザ操作（playwright-cli）

プロジェクトローカルにインストール済み。グローバルインストールではなく `pnpm exec` 経由で使うこと。

```bash
pnpm exec playwright-cli open http://localhost:5173/
pnpm exec playwright-cli snapshot
pnpm exec playwright-cli close
```
