# AI Orchestration 強制路由與 Copilot 指引

## 流程卡判定句（條件不符時顯示警告）

1. 任何任務 → 必派 `workflow_navigator`
2. 任務完成 → 必派 `audit_inspector`
3. 依任務屬性加派專門 gate
4. 須留存 `GLOBAL-EVIDENCE-005` 證據字樣

## 規範條款 ID

- POLICY-DRIFT-001
- POLICY-METRIC-002
- GLOBAL-ROUTING-003
- GLOBAL-AUDIT-004
- GLOBAL-EVIDENCE-005
- GLOBAL-AOP-006
