# 工作記憶快照

**時間**: 2026-05-30
**範圍**: EZRetrofit 程式碼品質與安全性修復 (001-fix-ezretrofit-code-quality)

## 關鍵進度
1. **任務清單建立**：基於 `plan.md` 與 `spec.md` 產出 `tasks.md`，共 25 項任務，區分 Setup、Foundational 及 5 個 User Story。MVP 定義為 US1 (Cookie 過期清除修正)。
2. **專案憲章建立**：基於資安審查與需求，產出 `constitution.md` (v1.0.0)，確立五大核心原則：
   - Thread-Safety First (禁止可變 Singleton)
   - Secure by Default (SHA-256 Pinning)
   - Test-First (TDD 強制)
   - Transparent Error Handling (嚴禁空 catch)
   - Backward Compatibility (API 變更保留 `@Deprecated`)

## 待辦事項 (TODOs)
- 開始執行 `tasks.md` 內的實作階段。
- 更新其他 SpecKit 模板以對齊新的 Constitution 規範。
