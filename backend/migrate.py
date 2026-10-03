import os
import shutil
import re

base_dir = r"C:\Users\meetp\.gemini\antigravity\scratch\BIT-41\backend"
monolith_dir = os.path.join(base_dir, "monolith")
monolith_src = os.path.join(monolith_dir, "src", "main", "java", "com", "bit41", "monolith")
monolith_res = os.path.join(monolith_dir, "src", "main", "resources")

os.makedirs(monolith_src, exist_ok=True)
os.makedirs(monolith_res, exist_ok=True)

# Package mapping
services = ['catalog-service', 'inventory-service', 'notification-service', 'order-service', 'payment-service']

def replace_package_and_imports(content):
    # Replace package com.bit41.<svc>.<domain> -> package com.bit41.monolith.<domain>
    # Wait, some are com.bit41.catalogservice...
    content = re.sub(r'package\s+com\.bit41\.[a-z]+(\.[a-zA-Z0-9_]+)?;', 
                     lambda m: f"package com.bit41.monolith{m.group(1) if m.group(1) else ''};", content)
    
    content = re.sub(r'import\s+com\.bit41\.[a-z]+(\.[a-zA-Z0-9_]+)*\.([A-Z][a-zA-Z0-9_]*);',
                     lambda m: f"import com.bit41.monolith{m.group(1) if m.group(1) else ''}.{m.group(2)};", content)
    return content

for svc in services:
    svc_java_dir = os.path.join(base_dir, svc, "src", "main", "java", "com", "bit41")
    for root, dirs, files in os.walk(svc_java_dir):
        for file in files:
            if file.endswith(".java") and not "Application" in file and not "RabbitMQConfig" in file and not "SagaEvent" in file and not "ProcessedEvent" in file:
                full_path = os.path.join(root, file)
                
                # Determine target directory
                # Example root: ...\catalog-service\src\main\java\com\bit41\catalogservice\model
                # We want: ...\monolith\model
                
                rel_path = os.path.relpath(root, svc_java_dir)
                parts = rel_path.split(os.sep)
                # First part is catalogservice, second part is model/controller/etc
                sub_pkg = parts[1] if len(parts) > 1 else ""
                
                target_dir = os.path.join(monolith_src, sub_pkg)
                os.makedirs(target_dir, exist_ok=True)
                
                with open(full_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                content = replace_package_and_imports(content)
                
                # Some manual tweaks might be needed later, like removing rabbitmq stuff
                with open(os.path.join(target_dir, file), 'w', encoding='utf-8') as f:
                    f.write(content)

print("Migration done")
