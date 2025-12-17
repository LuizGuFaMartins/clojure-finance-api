(ns clojure-finance-api.domain.services.login-service
  (:require
    [clojure-finance-api.infra.auth.auth :as auth]
    [clojure-finance-api.domain.services.user-service :as user-service]))

(defn authenticate
  [ctx {:keys [email password]}]
  (let [user (user-service/find-user-by-email ctx email)]
    (cond
      (nil? user)
      {:error :user-not-found}

      (not (= password (:password user)))
      {:error :invalid-password}

      :else
      (let [token (auth/create-token user)]
        {:success {:access-token token :user (dissoc user :password)}}))))