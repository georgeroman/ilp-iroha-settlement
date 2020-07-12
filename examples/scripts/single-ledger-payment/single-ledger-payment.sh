#!/bin/bash

printf "Sending transaction for depositing assets into Alice's Iroha account...\n"
./helpers/add-asset-quantity.py

printf "Setting up the accounts...\n"
../setup-accounts/setup-accounts.sh

printf "Sending a payment from Alice to Bob...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://alice-node:7770 pay alice \
    --auth in_alice \
    --amount 500 \
    --to http://bob-node:8770/accounts/bob/spsp

sleep 10

printf "Checking Alice's Iroha balances...\n"
./helpers/check-balances.py "alice@test"

printf "Checking Bob's Iroha balances...\n"
./helpers/check-balances.py "bob@test"
