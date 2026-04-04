---
status: "accepted"
date: 2026-04-04
decision: "skillsはskills-lock.json + pnpm exec skills experimental_installで管理し、.claude/skillsのシンボリックリンクのみgit管理する"
superseded-by: ""
---

# Skills管理をskills-lock.jsonとシンボリックリンクで行う

## Context and Problem Statement

Claude Codeのskills（adr, context7等）をプロジェクトで利用するにあたり、skillsの実体ファイルをどのように管理するかを決める必要がある。skillsの実体は外部リポジトリから取得されるもので、サイズも大きく頻繁に更新される可能性がある。

## Decision Drivers

* skillsの実体ファイルをgitで直接管理するとリポジトリが肥大化する
* `pnpm exec skills experimental_install`でskills-lock.jsonから再現可能にインストールできる
* `.claude/skills`配下のシンボリックリンクはexperimental_installでは復元されない

## Considered Options

* skillsの実体ファイルをすべてgitで管理する
* skills-lock.json + experimental_installで管理し、シンボリックリンクのみgit管理する

## Decision Outcome

Chosen option: "skills-lock.json + experimental_installで管理し、シンボリックリンクのみgit管理する", because リポジトリの肥大化を防ぎつつ、lock fileによる再現性を確保できるため。

### Consequences

* Good, because skillsの実体ファイルがgit管理対象外となりリポジトリがクリーンに保たれる
* Good, because skills-lock.jsonにより環境間で同一バージョンのskillsを再現できる
* Bad, because クローン後に`pnpm exec skills experimental_install`の実行が必要
* Bad, because `.claude/skills`のシンボリックリンクは手動で`ln -s`する必要がある（experimental_installでは復元されないため）

### Confirmation

* `skills-lock.json`がgit管理されていること
* `.claude/skills`配下のシンボリックリンクがgit管理されていること
* `.agents/skills`（実体）が`.gitignore`に含まれていること
