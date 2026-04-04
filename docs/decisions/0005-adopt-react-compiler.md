---
status: "accepted"
date: 2026-04-04
decision: "React Compilerを採用し自動メモ化を有効にする"
superseded-by: ""
---

# React Compiler を採用し自動メモ化を有効にする

## Context and Problem Statement

React アプリケーションのパフォーマンス最適化において、`useMemo`・`useCallback`・`React.memo` の手動管理はコードの複雑さを増す。
React Compiler（旧React Forget）が正式リリース（v1.0.0）されたため、自動メモ化の採用を検討する。

## Decision Drivers

- 手動メモ化のボイラープレートを削減したい
- パフォーマンスの最適化を自動化したい
- React 19 との互換性
- ビルドパイプラインへの統合が容易であること

## Considered Options

1. React Compiler を採用する
2. 手動メモ化（useMemo / useCallback / React.memo）のみ

## Decision Outcome

Chosen option: "React Compilerを採用する", because v1.0.0として安定版がリリースされており、`@vitejs/plugin-react` v6 が `reactCompilerPreset` を公式にエクスポートしているため、Vite 8との統合が標準的な方法で行えるため。

### Confirmation

- `pnpm build` でReact Compilerがエラーなく動作すること
- ビルド出力のJSにコンパイラが生成したメモ化コードが含まれること

## Pros and Cons of the Options

### React Compiler を採用する

- Good, because `useMemo` / `useCallback` / `React.memo` の手動記述が不要になりコードがシンプルになる
- Good, because コンパイラが最適なメモ化を自動判断するため人為的なミスが減る
- Good, because `@vitejs/plugin-react` v6 + `@rolldown/plugin-babel` で公式サポートされた統合方法がある
- Neutral, because コンパイラのルールに従ったReactコード（副作用のない純粋なコンポーネント）が求められる
- Bad, because ビルド時間がわずかに増加する（Babelトランスフォームが追加されるため）

### 手動メモ化のみ

- Good, because ビルドパイプラインに追加の依存がない
- Bad, because 開発者が適切なメモ化判断を都度行う必要がある
- Bad, because メモ化の過不足によるパフォーマンス問題が発生しやすい

## Consequences

- Good, コンポーネントの不要な再レンダリングが自動で防止される
- Good, 新しいコードで `useMemo` / `useCallback` を意識する必要がなくなる
- Bad, `@rolldown/plugin-babel` が追加の依存として必要になる

## More Information

- Vite 8 + `@vitejs/plugin-react` v6 では `reactCompilerPreset()` を `@rolldown/plugin-babel` の preset として渡す方式
- `babel-plugin-react-compiler` v1.0.0 を使用
