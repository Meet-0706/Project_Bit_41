#!/bin/bash
curl -i -X POST https://project-bit-41.vercel.app/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"customer@test.com","password":"customer123"}'
