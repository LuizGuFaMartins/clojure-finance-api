(ns clojure-finance-api.domain.services.bank-data-service
  (:require
    [clojure-finance-api.domain.repositories.bank-data-repo :as repo]
    [clojure-finance-api.infra.http.context-utils :as ctx-utils]))

(defn list-bank-data
  [ctx]
  (try
    (let [conn (ctx-utils/get-db ctx)
          data (repo/list-bank-data conn)]
      {:success data})
    (catch Exception _
      {:error :database-error})))

(defn find-bank-data-by-id
  [ctx id]
  (try
    (let [conn (ctx-utils/get-db ctx)]
      (if-let [bank-data (repo/find-bank-data-by-id conn id)]
        {:success bank-data}
        {:error :bank-data-not-found}))
    (catch Exception _
      {:error :database-error})))

(defn find-bank-data-by-user-id
  [ctx id]
  (try
    (let [conn (ctx-utils/get-db ctx)
          data (repo/find-bank-data-by-user-id conn id)]
      {:success data})
    (catch Exception _
      {:error :database-error})))

(defn create-bank-data
  [ctx body]
  (try
    (let [conn (ctx-utils/get-db ctx)
          user-id (ctx-utils/get-user-id ctx)
          id (random-uuid)
          bank-data (merge {:id id :user_id user-id :active true :balance 0} body)]
      (repo/create-bank-data! conn bank-data)
      {:success bank-data})
    (catch Exception _
      {:error :database-error})))

(defn update-bank-data
  [ctx id body]
  (try
    (let [conn (ctx-utils/get-db ctx)]
      (if-let [current-data (repo/find-bank-data-by-id conn id)]
        (do
          (repo/update-bank-data! conn id body)
          {:success (merge current-data body)})
        {:error :bank-data-not-found}))
    (catch Exception _
      {:error :database-error})))

(defn delete-bank-data
  [ctx id]
  (try
    (let [conn (ctx-utils/get-db ctx)]
      (if (repo/find-bank-data-by-id conn id)
        (do
          (repo/delete-bank-data! conn id)
          {:success nil})
        {:error :bank-data-not-found}))
    (catch Exception _
      {:error :database-error})))