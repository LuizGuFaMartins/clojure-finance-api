(ns clojure-finance-api.domain.services.bank-data-service
  (:require
    [clojure-finance-api.domain.repositories.bank-data-repo :as repo]))

(defn list-bank-data
  [{{:keys [datasource]} :components}]
  (try
    (let [data (repo/list-bank-data datasource)]
      {:success data})
    (catch java.lang.Exception _
      {:error :database-error})))

(defn find-bank-data-by-id
  [{{:keys [datasource]} :components} id]
  (try
    (if-let [bank-data (repo/find-bank-data-by-id datasource id)]
      {:success bank-data}
      {:error :bank-data-not-found})
    (catch java.lang.Exception _
      {:error :database-error})))

(defn find-bank-data-by-user-id
  [{{:keys [datasource]} :components} id]
  (try
    (let [data (repo/find-bank-data-by-user-id datasource id)]
      {:success data})
    (catch java.lang.Exception _
      {:error :database-error})))

(defn create-bank-data
  [{{:keys [datasource]} :components} body]
  (try
    (let [id (random-uuid)
          bank-data (merge {:id id :active true :balance 0} body)]
      (repo/create-bank-data! datasource bank-data)
      {:success bank-data})
    (catch java.lang.Exception _
      {:error :database-error})))

(defn update-bank-data
  [{{:keys [datasource]} :components} id body]
  (try
    (if-let [current-data (repo/find-bank-data-by-id datasource id)]
      (do
        (repo/update-bank-data! datasource id body)
        {:success (merge current-data body)})
      {:error :bank-data-not-found})
    (catch java.lang.Exception _
      {:error :database-error})))

(defn delete-bank-data
  [{{:keys [datasource]} :components} id]
  (try
    (if (repo/find-bank-data-by-id datasource id)
      (do
        (repo/delete-bank-data! datasource id)
        {:success nil})
      {:error :bank-data-not-found})
    (catch java.lang.Exception _
      {:error :database-error})))