(ns clojure-finance-api.domain.services.user-service
  (:require
    [clojure-finance-api.domain.repositories.user-repo :as repo]
    [clojure-finance-api.infra.auth.hash :as hash-util]))

(defn list-users
  [{{:keys [datasource]} :components {:keys [identity]} :request}]
  (let [logged-id  (:id identity)
        users (repo/list-users datasource)]
    (cond
      (empty? users)
      {:error :has-no-users}

      :else
      (let [filtered-users (->> users
                                (remove #(= (str (:id %)) (str logged-id)))
                                (map #(dissoc % :password)))]
        {:success {:users filtered-users}}))))

(defn find-user-by-id
  [{{:keys [datasource]} :components} id]
  (let [user (repo/find-user-by-id datasource id)]
    (if user
      {:success (dissoc user :password)}
      {:error :user-not-found})))

(defn find-user-by-email-with-password
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

          hashed-password (hash-util/hash-password (:password body))
          user (merge {:id id :active true :balance 0}
                      body
                      {:password hashed-password})]
      (try
        (repo/create-user! datasource user)
        {:success (dissoc user :password)}
        (catch Exception e
          (println "Erro DB:" (.getMessage e))
          {:error :database-error})))))

(defn update-user
  [{{:keys [datasource]} :components} id body]
  (let [user (repo/find-user-by-id datasource id)]
    (if-not user
      {:error :user-not-found}
      (let [updated-body (if (:password body)
                           (assoc body :password (hash-util/hash-password (:password body)))
                           body)]
        (try
          (repo/update-user! datasource id updated-body)
          {:success (dissoc (merge user updated-body) :password)}
          (catch Exception e
            (println "Erro DB:" (.getMessage e))
            {:error :database-error}))))))

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