#!/usr/bin/python3

import os
import sys

from iroha import Iroha, IrohaCrypto, IrohaGrpc

iroha = Iroha("alice@test")
net = IrohaGrpc("localhost:50051")

alice_key = open(os.path.dirname(__file__) + "/../../../iroha-data/alice@test.priv", "r").read()

query = iroha.query(
    "GetAccountAssets",
    account_id=sys.argv[1]
)

IrohaCrypto.sign_query(query, alice_key)
response = net.send_query(query)

for asset in response.account_assets_response.account_assets:
    print("asset_id = {}, balance = {}".format(asset.asset_id, asset.balance))
