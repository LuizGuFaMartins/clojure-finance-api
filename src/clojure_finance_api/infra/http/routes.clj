(ns clojure-finance-api.infra.http.routes
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [clojure-finance-api.infra.auth.jwt :as auth]
            [clojure-finance-api.shared.global-interceptors :as interceptors]
            [clojure-finance-api.infra.interceptors.user-interceptors :as user-i]
            [clojure-finance-api.infra.interceptors.login-interceptors :as login-i]
            [clojure-finance-api.infra.interceptors.bank-data-interceptors :as bank-i]))

(def raw-routes
  [;; --- Login ---
   ["/login" :post [(body-params/body-params) login-i/login]
    :route-name :action-login
    :public true]

   ;; --- Users ---
   ["/users"     :get  [user-i/list-users-interceptor] :route-name :list-users]
   ["/users"     :post [(body-params/body-params) user-i/user-create-interceptor] :route-name :create-user]
   ["/users/:id" :get  [user-i/user-find-by-id-interceptor] :route-name :find-user-by-id]
   ["/users/:id" :put  [(body-params/body-params) user-i/user-update-interceptor] :route-name :update-user]
   ["/users/:id" :delete [user-i/user-delete-interceptor] :route-name :delete-user]

   ;; --- Bank Data ---
   ["/bank-data" :get  [bank-i/list-bank-data-interceptor] :route-name :list-bank-data]
   ["/bank-data" :post [(body-params/body-params) bank-i/bank-data-create-interceptor] :route-name :create-bank-data]
   ["/bank-data/user/:user-id" :get [bank-i/bank-data-find-by-user-id-interceptor] :route-name :find-bank-data-by-user]
   ["/bank-data/:id" :get    [bank-i/bank-data-find-by-id-interceptor] :route-name :find-bank-data-by-id]
   ["/bank-data/:id" :put    [(body-params/body-params) bank-i/bank-data-update-interceptor] :route-name :update-bank-data]
   ["/bank-data/:id" :delete [bank-i/bank-data-delete-interceptor] :route-name :delete-bank-data]])

(defn- wrap-auth-interceptor
  [routes]
  (map (fn [route]
         (let [
               [path method interceptors] route
               opts (apply hash-map (drop 3 route))
               is-public? (:public opts)
               clean-opts (dissoc opts :public)
               final-interceptors (if is-public?
                                    interceptors
                                    (into [auth/auth-interceptor interceptors/rls-interceptor] interceptors))]

           (vec (concat [path method final-interceptors] (mapcat identity clean-opts)))))
       routes))

(def routes
  (-> raw-routes
      wrap-auth-interceptor
      set
      route/expand-routes))