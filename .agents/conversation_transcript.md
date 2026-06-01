# 對話歷程紀錄指引 (Conversation Transcript Guidelines)

本文件說明在此工作區中對話歷程紀錄（conversation transcripts）的用途、格式與分析診斷方法。

## 1. 歷程紀錄位置與檔案說明

對話歷程紀錄保存在本機的絕對路徑下：
`<appDataDir>\brain\<conversation-id>\.system_generated\logs\`

在此目錄中包含以下兩個關鍵檔案：
- `transcript_full.jsonl`：完整且未經截斷的對話歷程紀錄，包含所有步驟的完整詳細內容。
- `transcript.jsonl`：經過 Token 最佳化、截斷處理的簡化版本，適合用於快速瀏覽與分析。

---

## 2. JSON Lines 中的關鍵欄位

這兩個檔案皆使用 JSON Lines (JSONL) 格式儲存，每一行代表對話中的一個步驟。每個步驟包含以下關鍵欄位：

- `step_index`：該動作步驟的索引編號。
- `source`：訊息來源，例如 `USER_EXPLICIT`（使用者直接輸入）、`MODEL`（AI 模型回應）、`SYSTEM`（系統訊息）。
- `type`：步驟類型，例如 `USER_INPUT`（使用者輸入）、`PLANNER_RESPONSE`（規劃器回應）。
- `status`：執行狀態，例如 `DONE`（成功完成）、`ERROR`（發生錯誤）。
- `content`：該步驟的文字內容或工具執行的輸出結果。
- `tool_calls`：該步驟中所呼叫的工具（Tools）陣列，若無呼叫則為空。

---

## 3. 診斷與分析對話歷程的常用 Shell 指令

您可以使用 PowerShell 或 bash 指令來查詢及診斷對話紀錄：

### 尋找派生的子代理 (Subagents)
若要檢查專案在執行過程中調度了哪些子代理，可以使用以下指令過濾 `invoke_subagent` 或相關工具呼叫：
```bash
grep "invoke_subagent" <appDataDir>\brain\<conversation-id>\.system_generated\logs\transcript.jsonl
```

### 尋找所有使用者訊息
若要快速查看使用者在對話中輸入的所有指令或問題：
```bash
grep '"type":"USER_INPUT"' <appDataDir>\brain\<conversation-id>\.system_generated\logs\transcript.jsonl
```

### 檢查 Session 開頭 (前 10 行)
若要快速檢查該對話 Session 的初始化狀態與開頭資訊：
```bash
head -n 10 <appDataDir>\brain\<conversation-id>\.system_generated\logs\transcript.jsonl
```

---
GLOBAL-EVIDENCE-005
