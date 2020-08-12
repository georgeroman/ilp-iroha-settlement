# Examples

Here you can find various examples illustrating the integration between [Hyperledger Iroha](https://github.com/hyperledger/iroha) and the [Interledger](https://interledger.org/) protocol.
The provided examples use the [interledger-rs](https://github.com/interledger-rs/interledger-rs) connector implementation.

- [`setup-accounts`](./scripts/setup-accounts): illustrates the process of setting up Interledger accounts within the connectors, which will trigger corresponding account setups within the settlement engines in order to exchange the ledger identifiers of the peers needed for performing settlements
- [`iroha-payment`](./scripts/iroha-payment): provides a very simple payment example made on a single underlying Iroha ledger and it involves the settlement engines for performing the settlement and for keeping the accounting system of the connectors up-to-date with the correct balances of the peers
- [`iroha-iroha-payment`](./scripts/iroha-iroha-payment): shows the process of making a payment between peers residing on different underlying Iroha ledgers, illustrating how payments can be routed across a network of peering Interledger connectors
- [`iroha-ethereum-payment`](./scripts/iroha-ethereum-payment): illustrates the process of sending a payment from an Iroha network, through Interledger, to an Ethereum blockchain

Watch out the logs for more detailed information on the interactions that occur between connectors, settlement engines and the underlying blockchain networks.
Also, [ILP-RFC 0038: Settlement Engines](https://github.com/interledger/rfcs/blob/master/0038-settlement-engines/0038-settlement-engines.md#settlement-engine-http-api) and [ILP-RFC 0032: Peering, Clearing and Settling](https://github.com/interledger/rfcs/blob/master/0032-peering-clearing-settlement/0032-peering-clearing-settlement.md) are good starting points for getting familiar with the underlying concepts these examples illustrate.

### Prerequisites

You must have `docker` and `docker-compose` installed in order to run these examples.

The required setup is powered by `docker-compose`, which sets up required blockchain networks, Interledger connectors and settlement engines all within a single docker network.
Start it up with the following:
- `docker-compose -f docker-compose-iroha.yml up`: for running the examples that only deal with a single underlying Iroha blockchain
- `docker-compose -f docker-compose-iroha-iroha up`: for running the example for making a payment across two different Iroha networks
- `docker-compose -f docker-compose-iroha-ethereum up`: for running the example for sending a payment from an Iroha network to an Ethereum blockchain.
