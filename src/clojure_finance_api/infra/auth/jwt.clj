
(ns clojure-finance-api.infra.auth.jwt
  (:require [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [io.pedestal.interceptor :refer [interceptor]]))

(def ^:private secret (or (System/getenv "JWT_SECRET") "mudar-para-uma-env-var-em-producao"))

(defn create-token [user]
  (let [now (java.time.Instant/now)
        payload {:id   (str (:id user))
                 :role (name (:role user))
                 :iat  (.getEpochSecond now)
                 :exp  (.getEpochSecond (.plus now 1 java.time.temporal.ChronoUnit/HOURS))
                 :aud  "clojure-finance-api"
                 :type "access"
                 :jti  (str (java.util.UUID/randomUUID))}] ;; ID único para este token
    (jwt/sign payload secret {:alg :hs256})))

(def auth-interceptor
  (interceptor
    {:name ::auth-interceptor
     :enter (fn [ctx]
              (let [auth-header (get-in ctx [:request :headers "authorization"])
                    token       (when (and auth-header (str/starts-with? auth-header "Bearer "))
                                  (subs auth-header 7))]
                (if-not token
                  (assoc ctx :response {:status 401
                                        :headers {"Content-Type" "application/json"}
                                        :body {:error "Token missing or malformed"}})
                  (try
                    (let [claims (jwt/unsign token secret {:alg :hs256 :aud "clojure-finance-api"})]
                      (if (= (:type claims) "access")
                        (assoc-in ctx [:request :identity] claims)
                        (assoc ctx :response {:status 401 :body {:error "Invalid token type"}})))

                    (catch Exception e
                      (let [error-msg (ex-message e)]
                        (assoc ctx :response
                                   {:status 401
                                    :headers {"Content-Type" "application/json"}
                                    :body {:error (cond
                                                    (str/includes? error-msg "exp") "Token expired"
                                                    (str/includes? error-msg "iat") "Invalid issue time"
                                                    :else "Invalid token")}})))))))}))

(defn authorize-role [required-role]
  (interceptor
    {:name ::authorize-role
     :enter (fn [ctx]
              (let [identity (get-in ctx [:request :identity])
                    user-role (:role identity)]
                (cond
                  (nil? identity)
                  (assoc ctx :response {:status 401 :body {:error "Unauthenticated"}})

                  (= (name required-role) (name user-role))
                  ctx

                  :else
                  (assoc ctx :response {:status 403 :body {:error "Forbidden"}}))))}))