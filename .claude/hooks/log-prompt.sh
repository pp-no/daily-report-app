#!/bin/bash
# UserPromptSubmit フック
# ユーザーが入力したプロンプトを .claude/prompt-log.md に追記する

INPUT=$(cat)

# jq でプロンプトテキストを抽出（なければ終了）
PROMPT=$(echo "$INPUT" | jq -r '.prompt // empty' 2>/dev/null)
if [ -z "$PROMPT" ]; then
  exit 0
fi

# 1行のみ・短すぎる入力（「はい」「ok」など）はスキップ
CHAR_COUNT=${#PROMPT}
if [ "$CHAR_COUNT" -lt 10 ]; then
  exit 0
fi

LOG_FILE=".claude/prompt-log.md"
DATE=$(date '+%Y-%m-%d')

# ログファイルが存在しない場合はヘッダーを作成
if [ ! -f "$LOG_FILE" ]; then
  printf "# プロンプトログ\n\n" > "$LOG_FILE"
fi

# 改行を除いて1行で記録
PROMPT_SINGLE=$(echo "$PROMPT" | tr '\n' ' ' | sed 's/  */ /g')
echo "- ${DATE}: ${PROMPT_SINGLE}" >> "$LOG_FILE"
