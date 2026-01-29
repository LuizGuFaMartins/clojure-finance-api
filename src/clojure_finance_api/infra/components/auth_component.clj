(ns clojure-finance-api.infra.components.auth-component
  (:require [com.stuartsierra.component :as component]
            [buddy.core.keys :as keys]
            [clj-http.client :as http]
            [cheshire.core :as json])
  (:import (java.math BigInteger)
           (java.security KeyFactory)
           (java.security.spec RSAPublicKeySpec)
           (java.util Base64)))

(defn- jwk-coord->bigint [coord]
  (BigInteger. 1 (.decode (Base64/getUrlDecoder) coord)))

(defn- jwk->public-key [{:keys [n e]}]
  (let [modulus  (jwk-coord->bigint n)
        exponent (jwk-coord->bigint e)
        spec     (RSAPublicKeySpec. modulus exponent)
        factory  (KeyFactory/getInstance "RSA")]
    (.generatePublic factory spec)))

(defrecord AuthComponent [config]
  component/Lifecycle
  (start [this]
    (println "Starting AuthComponent (API Client mode)")
    (let [auth-url (get-in config [:auth :auth-jwks-url])
          _        (println "Fetching public keys from:" auth-url)
          response (http/get auth-url {:as :json})
          jwks     (get-in response [:body :keys])
          public-keys (reduce (fn [m jwk]
                                (assoc m (:kid jwk) (jwk->public-key jwk)))
                              {}
                              jwks)]

      (if (seq public-keys)
        (assoc this :public-keys public-keys)
        (throw (ex-info "No Keys Found" {:url auth-url})))))

  (stop [this]
    (println "Stopping AuthComponent")
    (assoc this :public-keys nil)))

(defn new-auth-component [config]
  (map->AuthComponent {:config config}))