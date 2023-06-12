#!/bin/bash
curl -X 'PUT' \
  'http://localhost:8082/v1/maintenance' \
  -w "\n" \
  -H 'Content-Type: text/plain' \
  -d 'true' -v
