#!/usr/bin/python3

import os
import sys

from iroha import Iroha, IrohaCrypto, IrohaGrpc

# Iroha torii url
url = sys.argv[1]
# Iroha admin account that will send the transaction
admin_account = sys.argv[2]
# Iroha admin account private key path
admin_priv_path = sys.argv[3]
# Iroha asset
asset = sys.argv[4]
# Amount to be added to the account
amount = sys.argv[5]

iroha = Iroha(admin_account)
net = IrohaGrpc(url)

admin_key = open(admin_priv_path, "r").read()

tx = iroha.transaction([
    iroha.command(
        "AddAssetQuantity",
        asset_id=asset,
        amount=amount
    )
])

IrohaCrypto.sign_transaction(tx, admin_key)
net.send_tx(tx)

for status in net.tx_status_stream(tx):
    print(status)
