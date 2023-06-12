#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer2" client_secret="secret2" scope="openid book:v2:write" -p b | jq -r '.access_token'`

http --json post :8080/v2/books "Authorization: Bearer ${access_token}" title="Practising Quarkus" author="Antonio Goncalves" yearOfPublication:="2020"
