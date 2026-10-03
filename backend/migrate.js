const fs = require('fs');
const path = require('path');

const baseDir = "C:\\Users\\meetp\\.gemini\\antigravity\\scratch\\BIT-41\\backend";
const monolithDir = path.join(baseDir, "monolith");
const monolithSrc = path.join(monolithDir, "src", "main", "java", "com", "bit41", "monolith");
const monolithRes = path.join(monolithDir, "src", "main", "resources");

fs.mkdirSync(monolithSrc, { recursive: true });
fs.mkdirSync(monolithRes, { recursive: true });

const services = ['catalog-service', 'inventory-service', 'notification-service', 'order-service', 'payment-service'];

function replacePackageAndImports(content) {
    let replaced = content.replace(/package\s+com\.bit41\.[a-z]+(?:\.([a-zA-Z0-9_]+))?;/g, (match, p1) => {
        return `package com.bit41.monolith${p1 ? '.' + p1 : ''};`;
    });

    replaced = replaced.replace(/import\s+com\.bit41\.[a-z]+(?:\.([a-zA-Z0-9_]+))*\.([A-Z][a-zA-Z0-9_]*);/g, (match, p1, p2) => {
        return `import com.bit41.monolith${p1 ? '.' + p1 : ''}.${p2};`;
    });

    return replaced;
}

function walkSync(dir, callback) {
    if(!fs.existsSync(dir)) return;
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const filepath = path.join(dir, file);
        const stats = fs.statSync(filepath);
        if (stats.isDirectory()) {
            walkSync(filepath, callback);
        } else if (stats.isFile()) {
            callback(filepath);
        }
    }
}

for (const svc of services) {
    const svcJavaDir = path.join(baseDir, svc, "src", "main", "java", "com", "bit41");
    walkSync(svcJavaDir, (filepath) => {
        if (filepath.endsWith('.java') && 
            !filepath.includes('Application.java') && 
            !filepath.includes('RabbitMQConfig.java') && 
            !filepath.includes('SagaEvent.java') && 
            !filepath.includes('ProcessedEvent.java') &&
            !filepath.includes('ProcessedEventRepository.java')) {
            
            const relPath = path.relative(svcJavaDir, filepath);
            const parts = relPath.split(path.sep);
            
            let subPkg = "";
            if (parts.length > 2) {
                // e.g. catalogservice/model/Product.java
                subPkg = parts[1];
            }
            
            const targetDir = path.join(monolithSrc, subPkg);
            fs.mkdirSync(targetDir, { recursive: true });
            
            let content = fs.readFileSync(filepath, 'utf8');
            content = replacePackageAndImports(content);
            
            const targetFile = path.join(targetDir, path.basename(filepath));
            fs.writeFileSync(targetFile, content, 'utf8');
        }
    });
}
console.log("Migration done");
