## inter-ledger-payment

This example shows the process of making a payment between parties residing on different underlying Iroha ledgers - Alice and Bob - thus illustrating how payments are routed within an ILP network.

When a payment is made between peers that are not directly connected to one another, payment packets have to be routed through an ILP network of connectors.
Besides having the important role of routing the payments, these intermediate connectors are also responsible for exchanging between the traded currencies, that is receiving a given currency and paying the next connector in the route in a different currency.
What is important about this process is that, in the end, the sender is able to make a payment in his currency of choice while the receiver will get the equivalent amount in his own currency of choice.

The following diagram illustrates how payments are routed and settled in a setup involving two peers and an intermediator that routes payments between them.

![Routing a payment](./images/route-payment.svg)
