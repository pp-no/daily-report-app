# Design Tokens

daily-report-app のフロントエンドで使用しているデザイントークン一覧。

---

## Color

### Brand

| トークン名 | 値 | 用途 |
|---|---|---|
| `color.brand.primary` | `#3b82f6` | ボタン・アクティブ状態・アクセント |
| `color.brand.primary.bg` | `rgba(59,130,246,0.15)` | サイドバーのアクティブ項目背景 |

### Background

| トークン名 | 値 | 用途 |
|---|---|---|
| `color.bg.page` | `#f1f5f9` | ページ背景 |
| `color.bg.white` | `#ffffff` | カード・入力欄・ダイアログ背景 |
| `color.bg.sidebar` | `#1a2536` | サイドバー背景 |

### Text

| トークン名 | 値 | 用途 |
|---|---|---|
| `color.text.primary` | `#1e293b` | 見出し・本文（濃色） |
| `color.text.secondary` | `#64748b` | サブテキスト・ラベル補足 |
| `color.text.muted` | `#94a3b8` | プレースホルダー・日付・非アクティブ |
| `color.text.label` | `#374151` | フォームラベル |
| `color.text.sidebar.user` | `#cbd5e1` | サイドバーのユーザー名 |
| `color.text.white` | `#ffffff` | ボタン内テキスト・サイドバーアクティブ |

### Border

| トークン名 | 値 | 用途 |
|---|---|---|
| `color.border.default` | `#e2e8f0` | 入力欄・カード・区切り線 |
| `color.border.muted` | `#94a3b8` | 下書き保存ボタンなど控えめなボーダー |
| `color.border.sidebar` | `rgba(255,255,255,0.08)` | サイドバー内の区切り線 |

### Semantic

| トークン名 | 値 | 用途 |
|---|---|---|
| `color.error.text` | `#ef4444` | エラーメッセージ・削除ボタン |
| `color.error.bg` | `#fef2f2` | エラーメッセージ背景 |
| `color.error.border` | `#fecaca` | エラーメッセージボーダー |
| `color.success.text` | `#059669` | 公開バッジのテキスト |
| `color.success.bg` | `#d1fae5` | 公開バッジの背景 |
| `color.muted.bg` | `#f1f5f9` | 非公開バッジの背景 |

---

## Typography

### Font Size

| トークン名 | 値 | 用途 |
|---|---|---|
| `font.size.xs` | `11px` | バッジ・補足テキスト最小 |
| `font.size.sm` | `12px` | 日付・ロゴサブテキスト |
| `font.size.base` | `13px` | ラベル・サブテキスト・ボタン |
| `font.size.md` | `14px` | 本文・入力欄・ナビ |
| `font.size.lg` | `15px` | サイドバーロゴテキスト |
| `font.size.xl` | `20px` | ログイン画面タイトル |
| `font.size.2xl` | `22px` | ページタイトル（h1） |

### Font Weight

| トークン名 | 値 | 用途 |
|---|---|---|
| `font.weight.normal` | `500` | ラベル・ユーザー名 |
| `font.weight.semibold` | `600` | ボタン・カードタイトル・ロゴ |
| `font.weight.bold` | `700` | ページ見出し |

---

## Border Radius

| トークン名 | 値 | 用途 |
|---|---|---|
| `radius.sm` | `5px` | 編集・削除ボタン |
| `radius.md` | `6px` | 入力欄・通常ボタン・エラー表示 |
| `radius.lg` | `8px` | カード・ロゴアイコン・ダイアログ |
| `radius.xl` | `12px` | ログイン・登録画面カード |
| `radius.full` | `999px` | バッジ（pill 形状） |
| `radius.circle` | `50%` | アバター |

---

## Spacing

| トークン名 | 値 | 用途 |
|---|---|---|
| `spacing.1` | `4px` | アイコン padding など最小余白 |
| `spacing.2` | `8px` | ボタン間・アイコン余白 |
| `spacing.3` | `12px` | 入力欄 padding・カード内余白 |
| `spacing.4` | `16px` | フィールド間・モバイル padding |
| `spacing.5` | `20px` | サイドバー padding・セクション余白 |
| `spacing.6` | `24px` | ロゴエリア padding |
| `spacing.7` | `28px` | ページヘッダー下余白 |
| `spacing.8` | `32px` | デスクトップ padding |
| `spacing.10` | `40px` | デスクトップ横 padding |

---

## Shadow

| トークン名 | 値 | 用途 |
|---|---|---|
| `shadow.card` | `0 4px 24px rgba(0,0,0,0.08)` | ログイン・登録画面カード |
| `shadow.dialog` | `0 4px 24px rgba(0,0,0,0.15)` | 確認ダイアログ |

---

## Layout

| トークン名 | 値 | 用途 |
|---|---|---|
| `layout.sidebar.width` | `220px` | サイドバー固定幅 |
| `layout.content.maxWidth` | `900px` | 日報一覧のコンテンツ最大幅 |
| `layout.auth.maxWidth` | `380px` | ログイン・登録画面カードの最大幅 |

---

## Transition

| トークン名 | 値 | 用途 |
|---|---|---|
| `transition.fast` | `0.15s` | ボタンホバー・ナビアクティブ切替 |
| `transition.drawer` | `0.25s ease` | モバイルドロワーのスライドイン |
