import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// 靜態啟發式估算 (Fallback)
function fallbackCountTokens(text) {
    if (!text) return 0;
    
    // 1. 中文字元數 (含常用中文標點)
    const chineseChars = text.match(/[\u4e00-\u9fa5]/g) || [];
    const chineseCount = chineseChars.length;
    
    // 2. 英文單字數
    const englishWords = text.match(/[a-zA-Z]+/g) || [];
    const englishCount = englishWords.length;
    
    // 3. 其他非空白字元 (如數字、標點符號)
    const totalNonSpace = text.replace(/\s/g, '').length;
    const otherCount = Math.max(0, totalNonSpace - (chineseCount + englishWords.join('').length));
    
    // 啟發式權重公式
    const estimated = Math.round((chineseCount * 2) + (englishCount * 1.3) + (otherCount * 0.5));
    return estimated;
}

import { createRequire } from 'module';
const require = createRequire(import.meta.url);

// 主計數方法 (混合雙軌制)
export async function countTokens(text) {
    if (!text) return 0;
    
    try {
        const tiktokenPath = path.join(__dirname, '..', '..', 'mcp-token-server', 'node_modules', 'js-tiktoken', 'dist', 'index.cjs');
        
        if (fs.existsSync(tiktokenPath)) {
            const { getEncoding } = require(tiktokenPath);
            const encoding = getEncoding("cl100k_base");
            return encoding.encode(text).length;
        } else {
            throw new Error("找不到本地 js-tiktoken 套件路徑");
        }
    } catch (error) {
        // 載入失敗，平滑降級至靜態估算
        console.warn(`[WARNING] 無法使用動態 Tokenizer (${error.message})，已自動降級至靜態啟發式估算。`);
        return fallbackCountTokens(text);
    }
}

// 支援 CLI 呼叫
async function main() {
    const args = process.argv.slice(2);
    if (args.length === 0) return; // 作為 module 被 import 時不執行
    
    let text = "";
    if (args[0] === "--file" && args[1]) {
        const filePath = path.resolve(args[1]);
        if (fs.existsSync(filePath)) {
            text = fs.readFileSync(filePath, 'utf8');
        } else {
            console.error(`檔案不存在: ${filePath}`);
            process.exit(1);
        }
    } else {
        text = args.join(' ');
    }
    
    const count = await countTokens(text);
    console.log(count);
}

// 判斷是否為直接執行
if (process.argv[1] && (process.argv[1].endsWith('token_helper.js') || process.argv[1].endsWith('token_helper'))) {
    main().catch(err => {
        console.error(err);
        process.exit(1);
    });
}
