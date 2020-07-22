#!/bin/bash

printf "Sending transaction for depositing assets into Alice's Iroha account...\n"
../helpers/add-asset-quantity.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "coin0#test" "1000"

# Set up all accounts
../setup-accounts/setup-accounts.sh

printf "Sending a payment from Alice to Bob...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://alice-node:7770 pay alice \
    --auth in_alice \
    --amount 500 \
    --to http://bob-node:8770/accounts/bob/spsp

sleep 10

printf "Checking Alice's Iroha balances...\n"
../helpers/check-balances.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "alice@test"

printf "Checking Bob's Iroha balances...\n"
../helpers/check-balances.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "bob@test"
