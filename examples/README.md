# Examples

Here you can find various examples of `ilp-iroha-settlement`'s functionality:
- setup ILP accounts within the settlement engine and exchange ledger identifiers
- settle ILP payments on the underlying Iroha ledgers

All of these examples consist of interactions with ILP connectors, which will trigger corresponding actions on the settlement engine.
Watch out the logs for `alice-settlement` and `bob-settlement` entries.

### Prerequisites

You must have `docker` and `docker-compose` installed in order to run these examples.

The required setup is powered by the `docker-compose.yml` file, which sets up Iroha networks, ILP nodes and settlement engines all within a single docker network.
Start it up with `docker-compose up`.
