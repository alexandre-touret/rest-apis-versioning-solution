#!/bin/bash
curl -X 'PUT' \
  'http://localhost:8082/maintenance' \
  -w "\n" \
  -H 'Content-Type: text/plain' \
  -d 'false' -v
