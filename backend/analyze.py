import os
import glob
import re

services = ['auth-service', 'catalog-service', 'inventory-service', 'notification-service', 'order-service', 'payment-service']
base_dir = r"C:\Users\meetp\.gemini\antigravity\scratch\BIT-41\backend"

classes = []

for svc in services:
    svc_dir = os.path.join(base_dir, svc, "src", "main", "java")
    for root, dirs, files in os.walk(svc_dir):
        for file in files:
            if file.endswith(".java"):
                full_path = os.path.join(root, file)
                classes.append((svc, full_path, file))

for svc, path, file in classes:
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
        pkg_match = re.search(r'package\s+(.*?);', content)
        if pkg_match:
            print(f"{svc}: {file} -> {pkg_match.group(1)}")
