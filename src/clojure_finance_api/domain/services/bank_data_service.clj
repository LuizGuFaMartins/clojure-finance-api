(ns clojure-finance-api.domain.services.bank_data_service
  (:require
    [clojure-finance-api.domain.repositories.bank_data_repo :as repo]))

(defn list-bank-data
  [{{:keys [datasource]} :components}]
  (repo/list-bank-data datasource))

(defn find-bank-data-by-id
  [{{:keys [datasource]} :components} id]
  (repo/find-bank-data-by-id datasource id))

(defn find-bank-data-by-user-id
  [{{:keys [datasource]} :components} id]
  (repo/find-bank-data-by-user-id datasource id))

(defn create-bank-data
  [{{:keys [datasource]} :components} body]
  (let [bank-data (merge {:id (random-uuid) :active true :balance 0} body)]
    (repo/create-bank-data! datasource bank-data) bank-data))

(defn update-bank-data
  [{{:keys [datasource]} :components} id body]
  (repo/update-bank-data! datasource id body))

(defn delete-bank-data
  [{{:keys [datasource]} :components} id]
  (repo/delete-bank-data! datasource id))