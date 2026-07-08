const fs = require('fs');
const path = require('path');

// 检查Node.js版本
const nodeVersion = process.version;
const majorVersion = parseInt(nodeVersion.slice(1).split('.')[0]);

console.log(`当前Node.js版本: ${nodeVersion}`);

if (majorVersion < 20) {
    console.log('\n❌ Node.js版本不满足要求');
    console.log('需要: Node.js 20.19+ 或 22.12+');
    console.log('当前: Node.js ' + nodeVersion);
    console.log('\n解决方案:');
    console.log('1. 使用nvm切换版本: nvm use 20');
    console.log('2. 或安装新版本: nvm install 20');
    console.log('3. 或下载官网版本: https://nodejs.org/');
    
    // 临时修改package.json以降低版本要求
    try {
        const packagePath = path.join(__dirname, 'package.json');
        const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
        
        console.log('\n🔧 正在临时修改package.json以降低版本要求...');
        packageJson.engines.node = "^16.0.0";
        fs.writeFileSync(packagePath, JSON.stringify(packageJson, null, 2));
        console.log('✅ 已修改package.json，现在可以尝试运行 npm run dev');
        console.log('⚠️  但可能仍会遇到其他兼容性问题');
    } catch (error) {
        console.error('修改package.json失败:', error.message);
    }
} else {
    console.log('✅ Node.js版本满足要求');
}
