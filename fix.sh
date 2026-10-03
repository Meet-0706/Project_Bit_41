#!/bin/bash
sed -i 's/, schema = "[^"]*"//g' backend/monolith/src/main/java/com/bit41/monolith/model/*.java
