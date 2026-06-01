# AI Orchestration 路由固定規範（強制執行）

## AGENTS 兩層架構（2026-05-27 起）

| 檔案 | 用途 | 嵌入方式 |
|---|---|---|
| `AGENTS.md` | 規則 ID 索引 + 強制執行規範（精簡版） | 固定嵌入 System Prompt |
| `AGENTS_detail.md` | 完整細則、三層機制說明、子代理紀律 | **按需讀取**，有疑義時由 agent 主動讀取 |

> **Gate 最小讀取範圍**：`workflow_navigator` 僅需讀取 AGENTS.md 強制路由區段；`audit_inspector` 僅需讀取規範條款 ID 區段；`worklog_recorder` 僅需讀取工作日誌路徑規範。

---

## 範圍

本規範適用於本專案所有 AI 任務分派與 quality-gate 調度行為，屬強制規範。

## 全域強制路由與稽核（MUST，不得違背）

- **GLOBAL-ROUTING-003（全域 workflow 強制路由）**：所有任務（無論大小）在執行前 **必須** 先調度 `workflow_navigator`，不得跳過。
- **GLOBAL-AUDIT-004（全流程 audit 強制稽核）**：所有任務完成後 **必須** 調度 `audit_inspector`，對流程與證據鏈完成稽核。
- **GLOBAL-EVIDENCE-005（證據留存強制）**：每次任務至少需留存以下證據字樣：
  - `🔄 調度代理：workflow_navigator`
  - `✅/❌ 代理結果：workflow_navigator`
  - `🔄 調度代理：audit_inspector`
  - `✅/❌ 代理結果：audit_inspector`
  - `路由判斷卡`
- **GLOBAL-AOP-006（AOP workflow 不可跳過）**：當使用 `AI_captain` 作為調度代理時，所有使用者指示（含問答、分析、修改、提交）在執行前 **必須** 先調度 `workflow_navigator`，不得以任何理由豁免。
- **GLOBAL-CONCURRENCY-LIMIT-007（代理數量限制）**：當 `workflow_navigator` 每次調用 agents 時，同時間運作之 agents 數量不得超過連同 `AI_captain`, `workflow_navigator` 共 4 位 agents。
- **GLOBAL-SUBAGENT-FORMAT-008（子代理回傳規格）**：子代理回傳時必須遵守字數限制：
  - 路由判斷卡：正確時 ≤ 1 行；有缺失時 ≤ 150 tokens
  - 稽核報告：摘要 ≤ 200 tokens，詳細附件寫檔不回傳

## 三層機制（MUST）

### 1) 事前規則層

- 使用強制路由表決定「任務類型 -> 必派 quality-gate」。
- 未命中強制路由表時，才可進入人工判斷流程。

### 2) 事中決策層

- 每次調度前必填路由判斷卡（五欄）：
  1. 風險等級
  2. 影響範圍
  3. 相依數
  4. 證據需求
  5. 可回滾性

#### 流程卡固定判定句（MUST）

- 先問 1：是否為任何任務？若是，必派 `workflow_navigator`。
- 先問 2：任務是否完成？若是，必派 `audit_inspector`。
- 再問 3：任務屬性為何？依屬性加派專門 gate（如 `worklog_recorder`、`doc_custodian`、`implement_craftsman`、`test_sentinel`、`security_warden`）。
- 最後問 4：是否留存 `GLOBAL-EVIDENCE-005` 證據字樣？若否，視為未完成且不得提交。
- AOP 特別規則：只要本次由 `AI_captain` 調度，所有使用者指示一律先過 `workflow_navigator`。
- 唯一準則：全任務 workflow 前置 + audit 後置，再依任務屬性加派專門 gate，缺證據即不合規。

### 3) 事後稽核層

- `workflow_navigator`：流程路由與時機正確性。
- `audit_inspector`：證據鏈完整性與可追溯性。

## 強制路由表（節錄）

| 任務類型 | 必派代理 |
|---|---|
| 全部任務（全域） | `workflow_navigator` + `audit_inspector` |
| AI_captain 調度的所有使用者指示 | `workflow_navigator` + `audit_inspector` |
| 工作流程路由/時機 | `workflow_navigator` |
| 合規與證據鏈 | `audit_inspector` |
| 工作日誌新增/更新 | `worklog_recorder` |
| 文件一致性/規範落地 | `doc_custodian` |

## 規範條款 ID（MUST）

### POLICY-DRIFT-001：版本漂移防止

- spec/plan/tasks/contract 之間不得存在定義漂移。
- 若發現衝突，必須先修正文檔再執行任務。

### POLICY-METRIC-002：未量化詞彙管制

- 禁止以「一致、全體、完整、盡快」等未量化詞彙作為驗收標準。
- 所有驗收敘述必須轉為可量測 KPI/閾值（例如：覆蓋率 >= 90%、失敗率 = 0%、回應時間 <= 3 秒）。

## 強制執行機制

 1. 提交前必須通過 `scripts/enforce_orchestration_policy.py`。
 2. `.githooks/pre-commit` 會自動執行上述檢核。
 3. 本次有任何 staged 檔案時，必須至少有一份 staged 的 `workslog\*.md` 日誌，並包含 `GLOBAL-EVIDENCE-005` 證據字樣。
 4. 若本次 staged 檔案包含 `workslog\`，內容另必須包含：
    - `🔄 調度代理：worklog_recorder`
    - `路由判斷卡`

## 盤點流程規範（MUST）

 1. 一致性盤點預設採「同路徑、同檔名」逐檔比對。
 2. `AGENTS.md` 為通用版本，作為強制一致性基準。
 3. 盤點輸出必須明確標註：已套用「通用版排除規則」。
 
## 通用版排除規則

 1. 當工程師選擇 AI 工具為 `antigravity` 時，Orchestration policy 排除 `.github/` 目錄下的檢查。
 2. 當工程師選擇 AI 工具為 `copilot` 時，Orchestration policy 排除 `.agents/` 目錄下的檢查。

## 失敗處置

- 任一檢核失敗即阻擋提交。
- 需修正後重新執行提交流程。
