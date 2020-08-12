#!/bin/bash

printf "Adding Alice's account...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://alice-node:7770 accounts create alice \
    --auth alice_auth_token \
    --ilp-address example.alice \
    --asset-code "coin0#test" \
    --asset-scale 2 \
    --max-packet-amount 100 \
    --ilp-over-http-incoming-token in_alice \
    --settle-to 0

# This will trigger a settlement engine setup account action on Alice's side
printf "Adding Bob's account on Alice's node...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://alice-node:7770 accounts create bob \
    --auth alice_auth_token \
    --ilp-address example.bob \
    --asset-code "coin0#test" \
    --asset-scale 2 \
    --max-packet-amount 100 \
    --settlement-engine-url http://alice-settlement:3000 \
    --ilp-over-http-incoming-token bob_password \
    --ilp-over-http-outgoing-token alice_password \
    --ilp-over-http-url http://bob-node:8770/accounts/alice/ilp \
    --settle-threshold 500 \
    --min-balance -1000 \
    --settle-to 0 \
    --routing-relation Peer &

# This will trigger a settlement engine setup account action on Bob's side
printf "Adding Alice's account on Bob's node...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://bob-node:8770 accounts create alice \
    --auth bob_auth_token \
    --ilp-address example.alice \
    --asset-code "coin0#test" \
    --asset-scale 2 \
    --max-packet-amount 100 \
    --settlement-engine-url http://bob-settlement-0:3001 \
    --ilp-over-http-incoming-token alice_password \
    --ilp-over-http-outgoing-token bob_password \
    --ilp-over-http-url http://alice-node:7770/accounts/bob/ilp \
    --settle-threshold 500 \
    --min-balance -1000 \
    --settle-to 0 \
    --routing-relation Peer

# This will trigger a settlement engine setup account action on Bob's side
printf "Adding Charlie's account on Bob's node...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://bob-node:8770 accounts create charlie \
    --auth bob_auth_token \
    --asset-code "coin1#test" \
    --asset-scale 2 \
    --settlement-engine-url http://bob-settlement-1:3002 \
    --ilp-over-http-incoming-token charlie_password \
    --ilp-over-http-outgoing-token bob_other_password \
    --ilp-over-http-url http://charlie-node:9770/accounts/bob/ilp \
    --settle-threshold 500 \
    --min-balance -1000 \
    --settle-to 0 \
    --routing-relation Child &

printf "Adding Charlie's account...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://charlie-node:9770 accounts create charlie \
    --auth charlie_auth_token \
    --asset-code "coin1#test" \
    --asset-scale 2 \
    --ilp-over-http-incoming-token in_charlie \
    --settle-to 0

# This will trigger a settlement engine setup account action on Charlie's side
printf "Adding Bob's account on Charlie's node...\n"
docker run --rm --network examples_ilp-network interledgerrs/ilp-cli:latest \
    --node http://charlie-node:9770 accounts create bob \
    --auth charlie_auth_token \
    --ilp-address example.bob \
    --asset-code "coin1#test" \
    --asset-scale 2 \
    --settlement-engine-url http://charlie-settlement:3003 \
    --ilp-over-http-incoming-token bob_other_password \
    --ilp-over-http-outgoing-token charlie_password \
    --ilp-over-http-url http://bob-node:8770/accounts/charlie/ilp \
    --settle-threshold 500 \
    --min-balance -1000 \
    --settle-to 0 \
    --routing-relation Parent

sleep 10
