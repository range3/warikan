---
status: "accepted"
date: 2026-04-04
decision: "MADR 4.0.0で設計判断を記録する"
superseded-by: ""
---

# MADR 4.0.0 を設計判断の記録に採用する

## Context and Problem Statement

プロジェクトの設計判断が暗黙知として失われている。
新しいメンバーが「なぜこの技術を選んだのか」を理解できず、
同じ議論が繰り返されたり、過去の判断と矛盾する変更が行われている。

## Decision Drivers

- 開発者が慣れ親しんだMarkdownで書けること
- ソースコードと同じリポジトリで管理できること
- テンプレートが構造化されており、記入漏れを防げること
- コーディングエージェントが参照・解析しやすいこと

## Considered Options

1. MADR 4.0.0（Markdown Architectural Decision Records）
2. Nygard式ADR（オリジナルのシンプルなADR）
3. Y-Statement形式
4. 記録しない（現状維持）

## Decision Outcome

Chosen option: "MADR 4.0.0", because 構造化されたテンプレートにより
検討した選択肢のPros/Consが明示的に記録でき、チームメンバーや
コーディングエージェントが設計判断の文脈を正確に理解できるため。

### Confirmation

- `/adr レビュー` で定期的に整合性をチェック

## Pros and Cons of the Options

### MADR 4.0.0

- Good, because 選択肢のPros/Consが構造化されており人間にもコーディングエージェントにも解析しやすい
- Good, because 任意セクションマーカーにより判断の重要度に応じて詳細度を調整できる
- Neutral, because 公的な国際標準ではなくデファクトスタンダードである
- Bad, because Nygard式より記入するセクションが多い

### Nygard式ADR

- Good, because 非常にシンプルで書き始めのハードルが低い
- Bad, because 選択肢の比較評価セクションがなく判断根拠が不明確になりやすい

### Y-Statement形式

- Good, because 1文で判断を要約できる
- Bad, because ツールサポートが限定的

### 記録しない

- Good, because 追加作業が発生しない
- Bad, because 設計判断が失われ続ける

## Consequences

- Good, 設計判断の透明性が向上する
- Good, チームやコーディングエージェントが過去の判断を参照して一貫した意思決定を行える
- Bad, ADR作成に一定の作業時間が必要になる

## More Information

- [MADR 公式サイト](https://adr.github.io/madr/)
- [MADR GitHub リポジトリ](https://github.com/adr/madr)
