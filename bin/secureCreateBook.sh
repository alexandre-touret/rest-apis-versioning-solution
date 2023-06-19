#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer1" client_secret="secret1" scope="openid bookv1:write" -p b | jq -r '.access_token'`

http --json post :8080/v1/books "Authorization: Bearer ${access_token}" title="Practising Quarkus" authors:='[{"lastname":"Goncalves","firstname":"Antonio"}]' yearOfPublication:="2020"
