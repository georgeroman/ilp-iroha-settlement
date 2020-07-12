#!/usr/bin/python3

import os

from iroha import Iroha, IrohaCrypto, IrohaGrpc

iroha = Iroha("alice@test")
net = IrohaGrpc("localhost:50051")

alice_key = open(os.path.dirname(__file__) + "/../../../iroha-data/alice@test.priv", "r").read()

tx = iroha.transaction([
    iroha.command(
        "AddAssetQuantity",
        asset_id="coin#test",
        amount="1000"
    )
])

IrohaCrypto.sign_transaction(tx, alice_key)
net.send_tx(tx)

for status in net.tx_status_stream(tx):
    print(status)
