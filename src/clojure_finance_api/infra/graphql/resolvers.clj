(ns clojure-finance-api.infra.graphql.resolvers
  (:require [clojure-finance-api.domain.repositories.transaction-repo :as repo]
            [clojure-finance-api.infra.http.context-utils :as ctx-utils]))

(defn transaction-by-id [context args value]
  (let [db (ctx-utils/get-db context)]
    (repo/find-transaction-by-id db (:id args))))

(defn my-transactions [context args value]
  (let [ctx (get-in context [:request :lacinia-app-context])
        db (ctx-utils/get-db ctx)
        user-id (:id (ctx-utils/get-identity ctx))]
    (repo/find-transactions-by-user-id db user-id)))

(defn create-transaction [context args _]
  (let [db (ctx-utils/get-db context)
        from-id (get-in context [:request :identity :id])
        input   (:input args)]
    (repo/create-transaction! db (assoc input :from_user from-id))))