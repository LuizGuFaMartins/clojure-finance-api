(ns clojure-finance-api.domain.services.login-service
  (:require
    [clojure-finance-api.domain.services.user-service :as user-service]
    [clojure-finance-api.infra.auth.jwt :as auth]
    [clojure-finance-api.infra.auth.hash :as hash-util]))

(defn authenticate
  [ctx {:keys [email password]}]
  (let [result (user-service/find-user-by-email-with-password ctx email)
        user (:success result)]
    (cond
      (nil? user)
      {:error :user-not-found}

      (not (hash-util/check-password password (:password user)))
      {:error :invalid-password}

      (not (:active user))
      {:error :user-inactive}

      :else
      (let [token (auth/create-token user)
            user-data (dissoc user :password)]
        {:success {:access-token token
                   :user user-data}}))))
