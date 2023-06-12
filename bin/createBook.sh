#! /bin/bash

http --json post :8080/v1/books  title="Practising Quarkus" author="Antonio Goncalves" yearOfPublication:="2020"
