(ns clojure-finance-api.infra.graphql.schemas.mutations)

(def transaction-mutations
  {:create_transaction
   {:type :Transaction
    :args {:input {:type '(non-null :TransactionInput)}}
    :resolve :mutation/create-transaction}})
