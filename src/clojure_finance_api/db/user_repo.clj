(ns clojure-finance-api.db.user-repo
    (:require [next.jdbc :as jdbc]
              [next.jdbc.result-set :as rs]
              [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-kebab-maps})

(defn list-users [ds]
  (jdbc/execute!
    ds
    (sql/format {:select :* :from :users})
    builder))

(defn find-user-by-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select [:*]
       :from   :users
       :where  [:= :id id]}) builder))

(defn create-user! [ds user]
  (jdbc/execute-one!
    ds
    (sql/format
      {:insert-into :users
       :values [(select-keys user [:id :name :email :password])]})
        builder))

(defn update-user! [ds id data]
  (jdbc/execute-one!
    ds
    (sql/format
      {:update :users
       :set    (assoc data :updated-at :%now)
       :where  [:= :id id]
       :returning [:id :name :email :active :balance :created-at :updated-at]})
    builder))

(defn delete-user! [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:delete-from :users
       :where [:= :id id]
       :returning [:id]})
    builder))