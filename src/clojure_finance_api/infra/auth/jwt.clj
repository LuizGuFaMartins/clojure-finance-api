(ns clojure-finance-api.infra.auth.jwt
  (:require [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [buddy.auth.backends :as backends]
            [io.pedestal.interceptor :refer [interceptor]]))

(def secret "mudar-para-uma-env-var-em-producao")

(def jwt-backend (backends/jws {:secret secret}))

(defn create-token [user]
  (let [payload {:id (:id user)
                 :role (:role user)
                 :exp (.plus (java.time.Instant/now) 1 java.time.temporal.ChronoUnit/HOURS)}]
    (jwt/sign payload secret)))

(def auth-interceptor
  (interceptor
    {:name ::auth-interceptor
     :enter (fn [ctx]
              (let [auth-header (get-in ctx [:request :headers "authorization"])
                    token (when (and auth-header (str/starts-with? auth-header "Bearer "))
                            (subs auth-header 7))]

                (if-not token
                  (assoc ctx :response {:status 401
                                        :headers {"Content-Type" "application/json"}
                                        :body {:error "Token missing or malformed"}})
                  (try
                    (let [claims (jwt/unsign token secret)]
                      (assoc-in ctx [:request :identity] claims))
                    (catch Exception e
                      (let [msg (.getMessage e)]
                        (assoc ctx :response {:status 401
                                              :headers {"Content-Type" "application/json"}
                                              :body {:error (if (str/includes? msg "exp")
                                                              "Token expired"
                                                              "Invalid token")}})))))))}))

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