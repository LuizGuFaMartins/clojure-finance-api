(ns clojure-finance-api.infra.graphql.schemas.input-types)

(def input-types
  {:TransactionInput
   {:fields
    {:to_user {:type '(non-null ID)}
     :amount  {:type '(non-null Float)}
     :status  {:type 'String}}}})
