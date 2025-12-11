(ns clojure-finance-api.db.user-repo
    (:require [next.jdbc :as jdbc]))

(defn create-user! [ds user]
      (jdbc/execute-one! ds
                         ["insert into users (id, name, email, password_hash)
      values (?, ?, ?, ?)"
                          (:id user) (:name user) (:email user) (:password-hash user)]))

(defn find-user-by-id [ds id]
      (jdbc/execute-one! ds
                         ["select * from users where id = ?" id]))
