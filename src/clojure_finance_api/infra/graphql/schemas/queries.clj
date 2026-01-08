(ns clojure-finance-api.infra.graphql.schemas.queries)

(def transaction-queries
  {:transaction_by_id
   {:type :Transaction
    :args {:id {:type '(non-null ID)}}
    :resolve :query/transaction-by-id}

   :my_transactions
   {:type '(list :Transaction)
    :resolve :query/my-transactions}})
