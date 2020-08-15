#!/bin/bash

printf "Sending transaction for depositing assets into Alice's Iroha0 account...\n"
../helpers/iroha-add-asset-quantity.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "coin0#test" "1000"

printf "Checking Alice's Iroha0 balances...\n"
../helpers/iroha-check-balances.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "alice@test"

printf "Checking Bob's Iroha0 balances...\n"
../helpers/iroha-check-balances.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "bob@test"

printf "Checking Bob's Ethereum balances...\n"
../helpers/ethereum-check-balances.py "http://localhost:8545" "0x3cDB3d9e1B74692Bb1E3bb5fc81938151cA64b02"

printf "Checking Charlie's Ethereum balances...\n"
../helpers/ethereum-check-balances.py "http://localhost:8545" "0x9b925641c5eF3fd86f63bfF2da55A0dEeAFD1263"

# Set up all accounts
./setup-accounts.sh

# All connectors must be aware of the exchange rate of the assets being exchanged
printf "Informing connectors about the exchange rates...\n"

curl --silent --output /dev/null --show-error \
    -X PUT -H 'Authorization: Bearer alice_auth_token' \
    -d '{"COIN0#TEST": 10, "WEI": 1}' \
    http://localhost:7770/rates

curl --silent --output /dev/null --show-error \
    -X PUT -H 'Authorization: Bearer bob_auth_token' \
    -d '{"COIN0#TEST": 10, "WEI": 1}' \
    http://localhost:8770/rates

curl --silent --output /dev/null --show-error \
    -X PUT -H 'Authorization: Bearer charlie_auth_token' \
    -d '{"COIN0#TEST": 10, "WEI": 1}' \
    http://localhost:9770/rates

printf "Sending a payment from Alice to Charlie...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://alice-node:7770 pay alice \
    --auth in_alice \
    --amount 50 \
    --to http://charlie-node:9770/accounts/charlie/spsp

sleep 10

printf "Checking Alice's Iroha0 balances...\n"
../helpers/iroha-check-balances.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "alice@test"

printf "Checking Bob's Iroha0 balances...\n"
../helpers/iroha-check-balances.py "localhost:50051" "alice@test" "../../iroha0-data/alice@test.priv" "bob@test"

printf "Checking Bob's Ethereum balances...\n"
../helpers/ethereum-check-balances.py "http://localhost:8545" "0x3cDB3d9e1B74692Bb1E3bb5fc81938151cA64b02"

printf "Checking Charlie's Ethereum balances...\n"
../helpers/ethereum-check-balances.py "http://localhost:8545" "0x9b925641c5eF3fd86f63bfF2da55A0dEeAFD1263"
