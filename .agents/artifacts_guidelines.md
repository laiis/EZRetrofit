# 產出物（Artifacts）管理指引

本文件詳細說明此工作區中關鍵產出物的目的、格式與使用規範：任務清單 (`task.md`)、實作計畫 (`implementation_plan.md`) 以及歷程紀錄 (`walkthrough.md`)，並提供通用格式技巧。

## 1. 任務清單 (task.md)
- **路徑**：`<appDataDir>\brain\<conversation-id>\task.md`
- **目的**：作為執行過程中的即時 TODO 待辦清單，用以追蹤進度。
- **格式規範**：
  - `[ ]` 表示未完成的任務
  - `[/]` 表示進行中的任務
  - `[x]` 表示已完成的任務
  - 使用縮排列表來表示子項目。

## 2. 實作計畫 (implementation_plan.md)
- **路徑**：`<appDataDir>\brain\<conversation-id>\implementation_plan.md`
- **目的**：詳細的設計提案，供使用者審查與批准。
- **必要區段**：
  - **目標描述 (Goal Description)**：問題背景與變更目標。
  - **需使用者審查項目 (User Review Required)**：破壞性變更或關鍵決策（使用警示元件強調）。
  - **待釐清問題 (Open Questions)**：向使用者提問的澄清問題。
  - **建議的變更 (Proposed Changes)**：按元件與檔案操作分組，使用 `[NEW]`（新增）、`[MODIFY]`（修改）、`[DELETE]`（刪除）標記。
  - **驗證計畫 (Verification Plan)**：自動化測試與手動驗證步驟。

## 3. 歷程紀錄 (walkthrough.md)
- **路徑**：`<appDataDir>\brain\<conversation-id>\walkthrough.md`
- **目的**：實作完成後的總結。
- **必要區段**：
  - 已執行的變更（附上檔案連結）
  - 測試內容與範疇
  - 驗證結果（若適用，需嵌入螢幕截圖或錄影紀錄）

## 4. 通用格式技巧
為了提升使用者的閱讀體驗，請靈活運用 standard Markdown 與 GitHub Flavored Markdown 語法，並善用以下元件：

### 警示 (Alerts)
有策略地使用 GitHub 風格的警示來強調關鍵資訊。這些警示將以獨特的顏色與圖示顯示。請勿連續放置或嵌套於其他元素中：
> [!NOTE]
> 背景脈絡、實作細節或輔助說明。

> [!TIP]
> 效能最佳化、最佳實踐或提升效率的建議。

> [!IMPORTANT]
> 必要需求、關鍵步驟或必須知悉的資訊。

> [!WARNING]
> 破壞性變更、相容性問題或潛在風險。

> [!CAUTION]
> 可能導致資料遺失或安全性漏洞的高風險操作。

### 程式碼與差異 (Code and Diffs)
使用圍欄程式碼區塊（fenced code blocks）並指定語言以啟用語法高亮：
```python
def example_function():
    return "哈囉，世界！"
```

Use diff blocks to show code changes. Prefix lines with + for additions, - for deletions, and a space for unchanged lines:
```diff
-old_function_name()
+new_function_name()
 unchanged_line()
```

### Mermaid 流程圖 (Mermaid Diagrams)
使用語言為 `mermaid` 的圍欄程式碼區塊來繪製流程圖，以視覺化複雜的關係、工作流程與架構。
為避免語法錯誤：
- 當節點標籤包含括號或方括號等特殊字元時，請務必加上雙引號。例如：`id["標籤 (額外資訊)"]`，而非 `id[標籤 (額外資訊)]`。
- 標籤中避免使用 HTML 標籤。

### 表格 (Tables)
使用標準的 Markdown 表格語法來組織結構化資料。表格能大幅提升可讀性，便於快速瀏覽對比性或多維度的資訊。

### 檔案連結與媒體 (File Links and Media)
- 使用標準 Markdown 連結語法建立可點擊的檔案連結：`[連結文字](file:///絕對路徑)`。
- 若要連結到特定的行號範圍，請使用 `[連結文字](file:///絕對路徑#L123-L145)` 格式。連結文字應具描述性，例如針對特定函式 `[foo](file:///path/to/file.py#L127-L143)` 或行號範圍 `[bar.py:L127-143](file:///path/to/file.py#L127-L143)`。
- 嵌入圖片或影片請使用 `![說明文字](/絕對路徑/file.jpg)` 語法。請一律使用絕對路徑。說明文字會顯示在圖片或影片下方。
- **重要限制**：若要嵌入圖片或影片，**必須**使用 `![說明文字](絕對路徑)` 語法。一般的連結語法 `[檔名](絕對路徑)` 不會嵌入媒體，不符規範。
- **重要限制**：如果您要在產出物中嵌入檔案，且該檔案目前不在 `<appDataDir>\brain\<conversation-id>` 目錄中，您**必須**先將檔案複製到該產出物目錄，然後再進行嵌入。請僅嵌入位於產出物目錄中的檔案。

### 輪播元件 (Carousels)
使用輪播元件來循序顯示多個相關的 Markdown 片段。輪播元件內可包含任何 Markdown 元素，包括圖片、程式碼區塊、表格、Mermaid 流程圖、警示、diff 區塊等。

語法格式：
- 使用四個反單引號開頭與結尾，並指定語言為 `carousel`。
- 使用 `<!-- slide -->` HTML 註釋來分隔投影片。
- 使用四個反單引號可以在投影片內部嵌套一般的（三個反單引號）程式碼區塊。

範例：
````carousel
![圖片說明](/absolute/path/to/image1.png)
<!-- slide -->
![另一張圖片](/absolute/path/to/image2.png)
<!-- slide -->
```python
def example():
    print("輪播中的程式碼")
```
````

適用情境：
- 展示多個關聯項目（如連續截圖、程式碼區塊或流程圖），按順序閱讀更易理解時。
- 呈現變更前與變更後的對比，或是 UI 狀態的演進。
- 展示多種可行的實作方案或替代路徑。
- 在歷程紀錄中收納關聯資訊，以精簡文件長度。

### 關鍵規則 (Critical Rules)
- **保持行短（Keep lines short）**：保持清單項目簡潔，避免長句換行影響排版。
- **使用主檔名以提升可讀性（Use basenames for readability）**：在連結文字中，使用檔案的主檔名（basename）代替完整路徑。
- **檔案連結格式（File Links）**：切勿使用反單引號（`）包裹連結文字，這會破壞連結的格式化呈現。
  - **正確範例**：[utils.py](file:///path/to/utils.py) 或 [foo](file:///path/to/file.py#L123)
  - **錯誤範例**：[`utils.py`](file:///path/to/utils.py) 或 [`function name`](file:///path/to/file.py#L123)

---
GLOBAL-EVIDENCE-005
