#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" scope="openid number:v1:read" -p b | jq -r '.access_token'`
echo $access_token
http :8080/v1/isbns "Authorization: Bearer ${access_token}"
