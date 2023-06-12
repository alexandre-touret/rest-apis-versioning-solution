#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer2" client_secret="secret2" scope="openid" -p b | jq -r '.access_token'`
echo $access_token
http :8080/isbns "X-API-VERSION: v1" "Authorization: Bearer ${access_token}"
