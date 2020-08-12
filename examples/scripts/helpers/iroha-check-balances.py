#!/usr/bin/python3

import os
import sys

from iroha import Iroha, IrohaCrypto, IrohaGrpc

# Iroha torii url
url = sys.argv[1]
# Iroha admin account that will perform the query
admin_account = sys.argv[2]
# Iroha admin account private key path
admin_priv_path = sys.argv[3]
# Iroha account to be queried
query_account = sys.argv[4]

iroha = Iroha(admin_account)
net = IrohaGrpc(url)

admin_key = open(admin_priv_path, "r").read()

query = iroha.query(
    "GetAccountAssets",
    account_id=query_account
)

IrohaCrypto.sign_query(query, admin_key)
response = net.send_query(query)

for asset in response.account_assets_response.account_assets:
    print("asset_id = {}, balance = {}".format(asset.asset_id, asset.balance))
