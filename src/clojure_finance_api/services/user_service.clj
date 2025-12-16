(ns clojure-finance-api.services.user-service
    (:require
      [clojure-finance-api.db.user-repo :as repo]))

(defn list-users
  [{{:keys [datasource]} :components}]
  (repo/list-users datasource))

(defn find-user-by-id
  [{{:keys [datasource]} :components} id]
  (repo/find-user-by-id datasource id))

(defn create-user
  [{{:keys [datasource]} :components} body]
  (let [user (merge {:id (random-uuid) :active true :balance 0} body)]
    (repo/create-user! datasource user) user))

(defn update-user
  [{{:keys [datasource]} :components} id body]
  (repo/update-user! datasource id body))

(defn delete-user
  [{{:keys [datasource]} :components} id]
  (repo/delete-user! datasource id))