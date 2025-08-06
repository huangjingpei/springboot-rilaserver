const fs = require('fs');
const path = require('path');

// 可能的文件路径
const possiblePaths = [
    'updates/test-platform/app-1.2.3-test.zip',
    'uploads/updates/test-platform/app-1.2.3-test.zip',
    'uploads/updates/1/Rila-LiveAssistant.exe',
    'updates/1/Rila-LiveAssistant.exe',
    'files/updates/1/Rila-LiveAssistant.exe',
    'static/updates/1/Rila-LiveAssistant.exe'
];

console.log('检查文件是否存在...\n');

possiblePaths.forEach(filePath => {
    const fullPath = path.resolve(filePath);
    const exists = fs.existsSync(fullPath);
    const stats = exists ? fs.statSync(fullPath) : null;
    
    console.log(`路径: ${filePath}`);
    console.log(`完整路径: ${fullPath}`);
    console.log(`存在: ${exists}`);
    if (exists) {
        console.log(`大小: ${stats.size} bytes`);
        console.log(`类型: ${stats.isFile() ? '文件' : '目录'}`);
    }
    console.log('---');
});

// 检查当前目录结构
console.log('\n当前目录结构:');
function listDir(dir, level = 0) {
    const indent = '  '.repeat(level);
    try {
        const items = fs.readdirSync(dir);
        items.forEach(item => {
            const itemPath = path.join(dir, item);
            const stats = fs.statSync(itemPath);
            const type = stats.isDirectory() ? '📁' : '📄';
            console.log(`${indent}${type} ${item}`);
            
            if (stats.isDirectory() && level < 2) {
                listDir(itemPath, level + 1);
            }
        });
    } catch (error) {
        console.log(`${indent}❌ 无法读取目录: ${error.message}`);
    }
}

listDir('.'); 