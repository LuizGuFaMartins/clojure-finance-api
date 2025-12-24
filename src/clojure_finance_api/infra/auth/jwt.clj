
(ns clojure-finance-api.infra.auth.jwt
  (:require [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [clojure-finance-api.infra.http.response :refer [response-error]]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.chain :as chain]))

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

(defn auth-error
  [ctx status message]
  (-> ctx
      (assoc :response (response-error status message))
      chain/terminate))

(def auth-interceptor
  (interceptor
    {:name ::auth-interceptor
     :enter
     (fn [ctx]
       (let [method (get-in ctx [:request :request-method])]
         (if (= method :options)
           ctx

           (let [auth-header (get-in ctx [:request :headers "authorization"])
                 header-token (when (and auth-header
                                         (str/starts-with? auth-header "Bearer "))
                                (subs auth-header 7))
                 cookies (get-in ctx [:request :cookies])
                 cookie-token (or (get-in cookies ["token" :value])
                                  (get-in cookies [:token :value]))
                 token (or header-token cookie-token)]

             (cond
               (str/blank? token)
               (auth-error ctx 401 "Token missing")

               :else
               (try
                 (let [claims (jwt/unsign token secret {:alg :hs256 :aud "clojure-finance-api"})]
                   (if (= (:type claims) "access")
                     (assoc-in ctx [:request :identity] claims)
                     (auth-error ctx 401 "Invalid token type")))
                 (catch Exception _
                   (auth-error ctx 401 "Invalid or expired token"))))))))}))

(defn authz-error
  [ctx status message]
  (-> ctx
      (assoc :response {:status status
                        :body {:error message}})
      chain/terminate))

(defn authorize-role [required-role]
  (interceptor
    {:name ::authorize-role
     :enter
     (fn [ctx]
       (let [identity (get-in ctx [:request :identity])
             user-role (:role identity)]
         (cond
           (nil? identity)
           (authz-error ctx 401 "Unauthenticated")

           (= (name required-role) (name user-role))
           ctx

           :else
           (authz-error ctx 403 "Forbidden"))))}))