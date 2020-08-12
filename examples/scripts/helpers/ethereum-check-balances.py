#!/usr/bin/python3

import os
import sys

from web3 import Web3

# Ethereum url
url = sys.argv[1]
# Ethereum account to be queried
account_addr = sys.argv[2]

w3 = Web3(Web3.HTTPProvider(url))

wei_balance = w3.eth.getBalance(account_addr)
eth_balance = w3.fromWei(wei_balance, "ether")

print("ETH balance: {}".format(eth_balance))
