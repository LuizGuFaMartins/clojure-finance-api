(ns clojure-finance-api.infra.graphql.schemas.queries)

(def transaction-queries
  {:my_transactions
    {:type '(list :Transaction)
     :args {:days {:type :Int}
            :type   {:type :String}}
     :resolve :query/my-transactions}

   :all_transactions
   {:type '(list :Transaction)
    :args {:days {:type :Int}
           :type   {:type :String}}
    :resolve :query/all-transactions}}
  )
