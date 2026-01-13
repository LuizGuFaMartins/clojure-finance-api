
(ns clojure-finance-api.infra.graphql.schemas.types)

(def transaction-types
  {:User
   {:fields
    {:id    {:type '(non-null ID)}
     :name {:type '(non-null String)}
     :email {:type '(non-null String)}}}

   :Transaction
   {:fields
    {:id         {:type '(non-null ID)}
     :amount     {:type '(non-null Float)}
     :status     {:type 'String}
     :created_at     {:type 'String}
     :from_user  {:type :User}
     :to_user    {:type :User}}}})
