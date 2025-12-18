(ns clojure-finance-api.domain.services.user-service
    (:require
      [clojure-finance-api.domain.repositories.user-repo :as repo]))

(defn list-users
  [{{:keys [datasource]} :components}]
  (let [users (repo/list-users datasource)]
    (cond
      (nil? users)
      {:error :has-no-users}
      :else
      {:success {:users users}})))

(defn find-user-by-id
  [{{:keys [datasource]} :components} id]
  (let [user (repo/find-user-by-id datasource id)]
    (if user
      {:success user}
      {:error :user-not-found})))

(defn find-user-by-email
  [{{:keys [datasource]} :components} email]
  (let [user (repo/find-user-by-email datasource email)]
    (if user
      {:success user}
      {:error :user-not-found})))

(defn create-user
  [{{:keys [datasource]} :components} body]
  (if (repo/find-user-by-email datasource (:email body))
    {:error :email-already-in-use}
    (let [id (random-uuid)
          user (merge {:id id :active true :balance 0} body)]
      (try
        (repo/create-user! datasource user)
        {:success user}
        (catch Exception _
          {:error :database-error})))))

(defn update-user
  [{{:keys [datasource]} :components} id body]
  (let [user (repo/find-user-by-id datasource id)]
    (if-not user
      {:error :user-not-found}
      (try
        (repo/update-user! datasource id body)
        {:success (merge user body)}
        (catch Exception _
          {:error :database-error})))))

(defn delete-user
  [{{:keys [datasource]} :components} id]
  (let [user (repo/find-user-by-id datasource id)]
    (if-not user
      {:error :user-not-found}
      (try
        (repo/delete-user! datasource id)
        {:success nil}
        (catch Exception _
          {:error :database-error})))))