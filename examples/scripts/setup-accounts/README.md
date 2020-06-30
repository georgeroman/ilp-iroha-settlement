## ILP Setup Accounts

When facilitating transactions between peers, ILP connectors need to keep track of **accounts**, which represent credits and debits for a set of transactions between counterparties.
These accounts are structured across the accounting and the settlement layer.
In the accounting layer, the accounts identify the ILP connectors that are responsible for proxying transactions.
In the settlement layer, the accounts identify the ledger addresses of the peers that are transacting.
More information on these concepts can be found at [ILP-RFC 0032: Peering, Clearing and Settling](https://github.com/interledger/rfcs/blob/master/0032-peering-clearing-settlement/0032-peering-clearing-settlement.md).

This particular example shows how ILP accounts are set up in the context of two transacting parties - Alice and Bob.

First, both Alice and Bob need to set up an account with an ILP connector.
Then, each ILP connector sets up an account with the counterparty's ILP connector.
This second step will, in turn, trigger an account setup at the settlement layer.
The settlement engines of the two ILP connectors will communicate via their ILP nodes and exchange ledger identifiers that are to be used for settling.

The following diagram shows the sequence of actions corresponding to Alice's side for exchanging ledger identifiers.
Bob's side is symmetric.
![Exchanging ledger identifiers](./images/exchange-ledger-ids.svg)
