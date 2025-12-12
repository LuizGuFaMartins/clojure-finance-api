(ns clojure-finance-api.db.user-repo
    (:require [next.jdbc :as jdbc]
              [next.jdbc.result-set :as rs]
              [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-kebab-maps})

(defn list-users [ds]
  (jdbc/execute! ds (-> {:select :* :from :users} (sql/format))))

(defn find-user-by-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select [:*]
       :from   :users
       :where  [:= :id id]})))

(defn create-user! [ds user]
  (println "BODY: " user)
  (jdbc/execute-one!
    ds
    (sql/format
      {:insert-into :users
       :values [(select-keys user [:id :name :email :password])]})
        builder))
