#! /bin/bash

http --json post :8080/books  "X-API-VERSION: v1" title="Practising Quarkus" author="Antonio Goncalves" yearOfPublication:="2020"
