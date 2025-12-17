(ns clojure-finance-api.infra.auth.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.auth :as ba]
            [buddy.auth.backends :as backends]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.error :refer [error-dispatch]]))

(def secret "mudar-para-uma-env-var-em-producao")

(def jwt-backend (backends/jws {:secret secret}))

(defn create-token [user]
  (let [payload {:id (:id user)
                 :role (:role user) ;; admin ou user
                 :exp (.plus (java.time.Instant/now) 1 java.time.temporal.ChronoUnit/HOURS)}]
    (jwt/sign payload secret)))

;(def auth-interceptor
;  (interceptor
;    {:name ::auth-interceptor
;     :enter (fn [context]
;              (let [request (:request context)
;                    token-data (ba/authenticate request jwt-backend)]
;                (if token-data
;                  (assoc-in context [:request :identity] token-data)
;                  (assoc context :response {:status 401 :body "Não autorizado"}))))}))

;(defn allow-only [role]
;  (interceptor
;    {:name ::allow-only
;     :enter (fn [context]
;              (let [user-role (get-in context [:request :identity :role])]
;                (if (= (name role) (name user-role))
;                  context
;                  (assoc context :response {:status 403 :body "Acesso proibido: Requer role " (name role)}))))}))