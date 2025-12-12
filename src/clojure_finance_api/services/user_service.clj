(ns clojure-finance-api.services.user-service
    (:require
      [clojure-finance-api.db.user-repo :as repo]
      [clojure-finance-api.domain :as domain]))

(defn list-users [{{:keys [datasource]} :components}]
    (let [users (repo/list-users datasource)] {:status 200 :body users}))

(defn create-user [{{:keys [datasource]} :components body :json-params}]
      (let [user (domain/new-user body)]
           (repo/create-user! datasource user)
           {:status 201 :body user}))

(defn respond-hello
      [request]
      {:status 200 :body "Hello world"})