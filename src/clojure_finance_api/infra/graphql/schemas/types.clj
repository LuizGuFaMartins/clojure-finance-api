
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
     :from_user  {:type :User
                  :resolve (fn [_ _ row]
                             {:id (:from_user_id row)
                              :name (:from_user_name row)
                              :email (:from_user_email row)})}

     :to_user    {:type :User
                  :resolve (fn [_ _ row]
                             {:id (:to_user_id row)
                              :name (:to_user_name row)
                              :email (:to_user_email row)})}}}})
