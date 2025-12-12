(ns clojure-finance-api.services.user-service
    (:require
      [clojure-finance-api.db.user-repo :as repo]))

(defn new-user [data]
  (merge {:id (random-uuid) :active true} data))

(defn list-users [{{:keys [datasource]} :components}]
    (let [users (repo/list-users datasource)] {:status 200 :body users}))

(defn create-user [{{:keys [datasource]} :components} body]
      (let [user (merge {:id (random-uuid) :active true} body)]
           (repo/create-user! datasource user)
           {:status 201 :body user}))
