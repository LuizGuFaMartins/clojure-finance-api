(ns clojure-finance-api.domain.repositories.bank-data-repo
    (:require [next.jdbc :as jdbc]
              [next.jdbc.result-set :as rs]
              [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-kebab-maps})

(defn list-bank-data [ds]
  (jdbc/execute!
    ds
    (sql/format
      {:select [:id :user-id :card-holder :card-last4 :card-brand
                :expires-month :expires-year :created-at]
       :from   :bank-data})
    builder))

(defn find-bank-data-by-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select [:id :user-id :card-holder :card-last4 :card-brand
                :expires-month :expires-year :created-at]
       :from   :bank-data
       :where  [:= :id id]})
    builder))

(defn find-bank-data-by-user-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select [:id :user-id :card-holder :card-last4 :card-brand
                :expires-month :expires-year :created-at]
       :from   :bank-data
       :where  [:= :user-id id]})
    builder))

(defn create-bank-data! [ds bank-data]
  (jdbc/execute-one!
    ds
    (sql/format
      {:insert-into :bank-data
       :values [(select-keys bank-data
                             [:id :user-id :card-holder :card-last4
                              :card-hash :card-brand
                              :expires-month :expires-year])]
       :returning [:id :user-id :card-holder :card-last4
                   :card-brand :expires-month :expires-year :created-at]})
    builder))

(defn update-bank-data! [ds id data]
  (jdbc/execute-one!
    ds
    (sql/format
      {:update :bank-data
       :set    data
       :where  [:= :id id]
       :returning [:id :user-id :card-holder :card-last4
                   :card-brand :expires-month :expires-year :created-at]})
    builder))

(defn delete-bank-data! [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:delete-from :bank-data
       :where [:= :id id]
       :returning [:id]})
    builder))
