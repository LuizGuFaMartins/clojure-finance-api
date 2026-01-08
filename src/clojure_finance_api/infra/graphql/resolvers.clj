(ns clojure-finance-api.infra.graphql.resolvers
  (:require [clojure-finance-api.domain.repositories.transaction-repo :as repo]))

(defn transaction-by-id [context args value]
  (let [db (get-in context [:request :database])]
    (repo/find-transaction-by-id db (:id args))))

(defn my-transactions [context args value]
  (let [db (get-in context [:request :database])
        user-id (get-in context [:request :identity :id])]
    (repo/find-transaction-by-user-id db user-id)))

(defn create-transaction [context args _]
  (let [db      (get-in context [:request :database])
        from-id (get-in context [:request :identity :id])
        input   (:input args)]
    (repo/create-transaction! db (assoc input :from_user from-id))))