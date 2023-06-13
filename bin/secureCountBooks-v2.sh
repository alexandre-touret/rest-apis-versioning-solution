#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer2" client_secret="secret2" scope="openid book:v2:read" -p b | jq -r '.access_token'`

http :8080/v2/books/count "Authorization: Bearer ${access_token}"
