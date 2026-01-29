
(ns clojure-finance-api.infra.graphql.schemas.types)

(def transaction-types
  {:User
   {:fields
    {:id    {:type '(non-null ID) :complexity 1}
     :name  {:type '(non-null String) :complexity 1}
     :email {:type '(non-null String) :complexity 1}}}

   :Transaction
   {:fields
    {:id         {:type '(non-null ID) :complexity 1}
     :amount     {:type '(non-null Float) :complexity 1}
     :status     {:type 'String :complexity 1}
     :created_at {:type 'String :complexity 1}

     :from_user  {:type :User
                  :complexity 5
                  :resolve (fn [_ _ row]
                             {:id (:from_user_id row)
                              :name (:from_user_name row)
                              :email (:from_user_email row)})}

     :to_user    {:type :User
                  :complexity 5
                  :resolve (fn [_ _ row]
                             {:id (:to_user_id row)
                              :name (:to_user_name row)
                              :email (:to_user_email row)})}}}})
