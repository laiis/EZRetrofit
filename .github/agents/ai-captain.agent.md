# AI Captain Agent 指引

## 三層機制固定規範（強制執行）

### 流程卡固定判定句（MUST）

- 先問 1：是否為任何任務
- 先問 2：任務是否完成
- 最後問 4：是否留存 `GLOBAL-EVIDENCE-005` 證據字樣
- 唯一準則：全任務 workflow 前置 + audit 後置，再依任務屬性加派專門 gate，缺證據即不合規。

## 規範條款 ID

- POLICY-DRIFT-001
- POLICY-METRIC-002
- GLOBAL-ROUTING-003
- GLOBAL-AUDIT-004
- GLOBAL-EVIDENCE-005
- GLOBAL-AOP-006
- GLOBAL-SUBAGENT-FORMAT-008
