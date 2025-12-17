(ns clojure-finance-api.domain.services.user-service
    (:require
      [clojure-finance-api.domain.repositories.user-repo :as repo]))

(defn list-users
  [{{:keys [datasource]} :components}]
  (repo/list-users datasource))

(defn find-user-by-id
  [{{:keys [datasource]} :components} id]
  (repo/find-user-by-id datasource id))

(defn find-user-by-email
  [{{:keys [datasource]} :components} email]
  (repo/find-user-by-email datasource email))

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