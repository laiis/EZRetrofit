#!/usr/bin/env python3
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

# 強制 Windows 主控台以 UTF-8 輸出，以避免 cp950 無法解析 ✅ 等 unicode 字元的問題
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8')

ROOT = Path(__file__).resolve().parents[1]


def detect_ai_tool() -> str:
    import os
    env_tool = os.environ.get("AI_TOOL") or os.environ.get("AI_TOOL_TYPE")
    if env_tool:
        return env_tool.lower().strip()

    if (ROOT / "opencode.jsonc").exists() or (ROOT / ".opencode").is_dir():
        return "opencode"
    if (ROOT / ".github" / "antigravity-instructions.md").exists():
        return "antigravity"
    if (ROOT / ".claudecode").exists():
        return "claudecode"
    if (ROOT / ".cursorrules").exists():
        return "cursor"
    if (ROOT / ".windsurfrules").exists():
        return "windsurf"
    if (ROOT / ".github" / "codex-instructions.md").exists():
        return "codex"

    return "copilot"


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def run_git(*args: str) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
        capture_output=True,
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "git command failed")
    return result.stdout


def ensure_tokens(path: Path, tokens: list[str]) -> list[str]:
    content = read_text(path)
    missing = [t for t in tokens if t not in content]
    return [f"{path.as_posix()} 缺少必要字樣：{t}" for t in missing]


def staged_files() -> list[str]:
    out = run_git("diff", "--cached", "--name-only")
    return [line.strip() for line in out.splitlines() if line.strip()]


def staged_content(path: str) -> str:
    # Read staged blob content.
    return run_git("show", f":{path}")


def main() -> int:
    errors: list[str] = []

    ai_tool = detect_ai_tool()
    if ai_tool == "antigravity":
        if (ROOT / ".agents" / "orchestration-routing-policy.md").exists():
            policy = ROOT / ".agents" / "orchestration-routing-policy.md"
        else:
            policy = ROOT / ".github" / "orchestration-routing-policy.md"
    else:
        policy = ROOT / ".github" / "orchestration-routing-policy.md"

    if ai_tool == "copilot":
        copilot = ROOT / ".github" / "copilot-instructions.md"
    elif ai_tool == "antigravity":
        if (ROOT / "GEMINI.md").exists():
            copilot = ROOT / "GEMINI.md"
        elif (ROOT / ".github" / "antigravity-instructions.md").exists():
            copilot = ROOT / ".github" / "antigravity-instructions.md"
        elif (ROOT / "AGENTS.md").exists():
            copilot = ROOT / "AGENTS.md"
        else:
            copilot = ROOT / ".github" / "copilot-instructions.md"
    elif ai_tool == "opencode":
        if (ROOT / "AGENTS.md").exists():
            copilot = ROOT / "AGENTS.md"
        else:
            copilot = ROOT / ".github" / "copilot-instructions.md"
    else:
        if ai_tool == "cursor" and (ROOT / ".cursorrules").exists():
            copilot = ROOT / ".cursorrules"
        elif ai_tool == "claudecode" and (ROOT / ".claudecode").exists():
            copilot = ROOT / ".claudecode"
        elif ai_tool == "windsurf" and (ROOT / ".windsurfrules").exists():
            copilot = ROOT / ".windsurfrules"
        elif ai_tool == "codex" and (ROOT / ".github" / "codex-instructions.md").exists():
            copilot = ROOT / ".github" / "codex-instructions.md"
        elif (ROOT / "AGENTS.md").exists():
            copilot = ROOT / "AGENTS.md"
        else:
            copilot = ROOT / ".github" / "copilot-instructions.md"

    if ai_tool == "antigravity":
        principal = ROOT / ".agents" / "agents" / "ai-captain.json"
        workflow_agent = ROOT / ".agents" / "agents" / "workflow-navigator.json"
    elif ai_tool == "opencode":
        principal = ROOT / ".opencode" / "agents" / "ai-captain.md"
        workflow_agent = ROOT / ".opencode" / "agents" / "workflow-navigator.md"
    else:
        principal = ROOT / ".github" / "agents" / "ai-captain.agent.md"
        workflow_agent = ROOT / ".github" / "agents" / "workflow-navigator.agent.md"

    file_rules = {
        policy: [
            "三層機制（MUST）",
            "流程卡固定判定句（MUST）",
            "先問 1：是否為任何任務",
            "先問 2：任務是否完成",
            "最後問 4：是否留存 `GLOBAL-EVIDENCE-005` 證據字樣",
            "唯一準則：全任務 workflow 前置 + audit 後置，再依任務屬性加派專門 gate，缺證據即不合規。",
            "GLOBAL-ROUTING-003",
            "GLOBAL-AUDIT-004",
            "GLOBAL-EVIDENCE-005",
            "GLOBAL-AOP-006",
            "GLOBAL-SUBAGENT-FORMAT-008",
            "事前規則層",
            "事中決策層",
            "事後稽核層",
            "POLICY-DRIFT-001",
            "POLICY-METRIC-002",
            "版本漂移",
            "未量化詞彙",
            "workflow_navigator",
            "audit_inspector",
            "worklog_recorder",
            "盤點流程規範（MUST）",
            "通用版排除規則",
            "AGENTS.md",
        ],
        copilot: [
            "AI Orchestration 強制路由",
            "流程卡判定句（條件不符時顯示警告）",
            "1. 任何任務 → 必派 `workflow_navigator`",
            "2. 任務完成 → 必派 `audit_inspector`",
            "4. 須留存 `GLOBAL-EVIDENCE-005` 證據字樣",
            "POLICY-DRIFT-001",
            "POLICY-METRIC-002",
            "GLOBAL-ROUTING-003",
            "GLOBAL-AUDIT-004",
            "GLOBAL-EVIDENCE-005",
            "GLOBAL-AOP-006",
        ],
        principal: [
            "三層機制固定規範（強制執行）",
            "流程卡固定判定句（MUST）",
            "先問 1：是否為任何任務",
            "先問 2：任務是否完成",
            "最後問 4：是否留存 `GLOBAL-EVIDENCE-005` 證據字樣",
            "唯一準則：全任務 workflow 前置 + audit 後置，再依任務屬性加派專門 gate，缺證據即不合規。",
            "POLICY-DRIFT-001",
            "POLICY-METRIC-002",
            "GLOBAL-ROUTING-003",
            "GLOBAL-AUDIT-004",
            "GLOBAL-EVIDENCE-005",
            "GLOBAL-AOP-006",
            "GLOBAL-SUBAGENT-FORMAT-008",
        ],
        workflow_agent: [
            "全域強制啟用規則（MUST）",
            "GLOBAL-AOP-006（AOP workflow 不可跳過）",
            "所有使用者指示（含問答、分析、修改、提交）",
        ],
    }

    for path, tokens in file_rules.items():
        if path is None:
            continue
        path_str = path.as_posix()
        if ai_tool == "antigravity" and "/.github/" in path_str:
            continue
        if ai_tool == "copilot" and "/.agents/" in path_str:
            continue
        if ai_tool == "opencode" and ("/.agents/" in path_str or "/.github/agents/" in path_str):
            continue

        if not path.exists():
            errors.append(f"{path.as_posix()} 不存在")
            continue
        errors.extend(ensure_tokens(path, tokens))

    try:
        staged = staged_files()
    except RuntimeError as exc:
        print(f"[ERROR] 無法讀取 staged 檔案：{exc}")
        return 2

    worklog_files = [p for p in staged if p.startswith("workslog/") and p.endswith(".md")]

    # GLOBAL-EVIDENCE-005: any commit with staged changes must have at least one
    # staged worklog containing workflow/audit evidence + routing card.
    if staged:
        if not worklog_files:
            errors.append("本次有 staged 變更，但無任何 staged 工作日誌（違反 GLOBAL-EVIDENCE-005）")
        else:
            combined = ""
            for wf in worklog_files:
                try:
                    combined += "\n" + staged_content(wf)
                except RuntimeError as exc:
                    errors.append(f"{wf} 無法讀取 staged 內容：{exc}")
            global_tokens = (
                "🔄 調度代理：workflow_navigator",
                "✅/❌ 代理結果：workflow_navigator",
                "🔄 調度代理：audit_inspector",
                "✅/❌ 代理結果：audit_inspector",
                "路由判斷卡",
            )
            for token in global_tokens:
                if token not in combined:
                    errors.append(f"工作日誌缺少全域稽核證據字樣：{token}")

    for wf in worklog_files:
        try:
            content = staged_content(wf)
        except RuntimeError as exc:
            errors.append(f"{wf} 無法讀取 staged 內容：{exc}")
            continue
        for token in ("🔄 調度代理：worklog_recorder", "路由判斷卡"):
            if token not in content:
                errors.append(f"{wf} 缺少必要證據字樣：{token}")

    if errors:
        print("[ERROR] Orchestration policy check failed:")
        for e in errors:
            print(f" - {e}")
        return 1

    print("[OK] Orchestration policy check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
