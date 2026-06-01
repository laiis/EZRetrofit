# 網頁應用程式開發指引 (Web Application Development Guidelines)

本文件概述了在此工作區中建立網頁應用程式的核心技術棧、設計美學、實作流程以及 SEO 最佳實踐。

## 技術棧 (Technology Stack)

1. **核心 (Core)**：使用 HTML 處理結構，使用 JavaScript 處理邏輯。
2. **樣式 CSS (Styling)**：使用原生 CSS (Vanilla CSS) 以獲得最大的靈活性與掌控力。除非有明確要求，否則避免使用 TailwindCSS；若明確要求使用，請務必先確認 TailwindCSS 的版本。
3. **網頁 App 框架 (Web App Framework)**：只有在明確要求開發複雜的網頁 App 時，才使用 Next.js 或 Vite 等框架。
4. **專案初始化命令規範 (New Project Creation)**：使用 `npx -y` 自動安裝指令碼與依賴套件。
   - 執行命令時，必須先加上 `--help` 旗標以檢視可用的選項。
   - 在當前目錄 `./` 初始化應用程式（例如：`npx -y create-vite-app@latest ./`）。
   - 以非互動模式 (non-interactive mode) 執行。
5. **本地運行與建置 (Running Locally & Building)**：使用 `npm run dev` 或等效的開發伺服器。只有在明確要求或驗證正確性時，才進行生產環境的打包建置 (Production Build)。

## 設計美學 (Design Aesthetics)

1. **精緻美學 (Rich Aesthetics)**：介面必須在視覺上令人驚艷，且第一眼就展現出高質感。運用現代網頁設計的最佳實踐（亮麗色彩、深色模式、磨砂玻璃效果/Glassmorphism、動態動畫）。
2. **視覺卓越 (Visual Excellence)**：
   - 避免使用單調無趣的預設顏色。使用精心調配、和諧的 HSL 調色盤，並搭配流暢的深色模式。
   - 使用現代字型（例如來自 Google Fonts 的 Inter、Roboto、Outfit 等），而非瀏覽器預設字型。
   - 套用平滑的漸層效果。
   - 實作細緻的微動畫 (Micro-animations) 以提升使用者體驗。
3. **動態互動設計 (Dynamic Design)**：確保擁有響應式版面配置 (Responsive Layouts)、懸停效果 (Hover Effects) 以及豐富的互動元素。
4. **高質感與防止預留佔位符 (Premium Feel & Anti-placeholder)**：除非另有要求，否則應避免製作簡陋的最小可行產品 (MVP)。切勿使用佔位符 (Placeholders)；請使用圖片生成工具來產生實際可用的資產。

## 實作流程 (Implementation Workflow)

1. **計畫與理解 (Plan and Understand)**：理解需求，研究現代化設計，規劃初始功能。
2. **建立底座 (Build Foundation)**：從修改 `index.css` 開始，以建立核心語意標記 (Design Tokens) 與工具類別 (Utilities)。
3. **元件開發 (Create Components)**：使用預先定義的樣式來開發專注且可重用的元件。
4. **頁面組裝 (Assemble Pages)**：更新主應用程式版面配置、路由、導覽列以及響應式設計。
5. **拋光與優化 (Polish and Optimize)**：審查使用者體驗、轉場流暢度與效能表現。

## SEO 最佳實踐 (SEO Best Practices)

- **標題標籤 (Title Tags)**：為每個頁面設定具描述性的標題標籤。
- **Meta 描述 (Meta Descriptions)**：撰寫引人入勝的摘要 Meta 標籤。
- **Heading 階層 (Heading Structure)**：每個頁面僅使用單一 `<h1>`，並保持清晰的 `<h2>` 至 `<h6>` 階層結構。
- **語意 HTML (Semantic HTML)**：使用 HTML5 語意標籤。
- **唯一識別碼 (Unique ID)**：為互動元素設定唯一且具描述性的 ID，以利於瀏覽器測試。
- **效能 (Performance)**：針對網頁載入速度進行優化。

---
GLOBAL-EVIDENCE-005
