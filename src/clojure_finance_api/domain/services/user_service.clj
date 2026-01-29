(ns clojure-finance-api.domain.services.user-service
  (:require
    [clojure-finance-api.domain.repositories.user-repo :as repo]
    [clojure-finance-api.infra.security.hash :as hash-util]
    [clojure-finance-api.infra.http.context-utils :as ctx-utils]))

(defn list-users
  [ctx]
  (let [conn      (ctx-utils/get-db ctx)
        logged-id (ctx-utils/get-user-id ctx)
        users     (repo/list-users conn)]
    (cond
      (empty? users)
      {:error :has-no-users}

      :else
      (let [filtered-users (->> users
                                (remove #(= (str (:id %)) (str logged-id)))
                                (map #(dissoc % :password)))]
        {:success {:users filtered-users}}))))

(defn find-user-by-id
  [ctx id]
  (let [conn (ctx-utils/get-db ctx)
        user (repo/find-user-by-id conn id)]
    (if user
      {:success (dissoc user :password)}
      {:error :user-not-found})))

(defn create-user
  [ctx body]
  (let [conn (ctx-utils/get-db ctx)]
    (if (repo/find-user-by-email conn (:email body))
      {:error :email-already-in-use}
      (let [id (random-uuid)
            hashed-password (hash-util/hash-password (:password body))
            user (merge {:id id :active true :balance 0}
                        body
                        {:password hashed-password})]
        (try
          (repo/create-user! conn user)
          {:success (dissoc user :password)}
          (catch Exception e
            (println "Erro DB:" (.getMessage e))
            {:error :database-error}))))))

(defn update-user
  [ctx id body]
  (let [conn (ctx-utils/get-db ctx)
        user (repo/find-user-by-id conn id)]
    (if-not user
      {:error :user-not-found}
      (let [payload (cond-> body
                            (:password body) (update :password hash-util/hash-password)
                            :always          (dissoc :id :created-at :email :role))]
        (try
          (repo/update-user! conn id payload)
          {:success (dissoc (merge user payload) :password)}
          (catch Exception e
            (println "Erro DB:" (.getMessage e))
            {:error :database-error}))))))

(defn delete-user
  [ctx id]
  (let [conn (ctx-utils/get-db ctx)
        user (repo/find-user-by-id conn id)]
    (if-not user
      {:error :user-not-found}
      (try
        (repo/delete-user! conn id)
        {:success nil}
        (catch Exception _
          {:error :database-error})))))