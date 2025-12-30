(ns clojure-finance-api.infra.http.routes
  (:require [clojure-finance-api.shared.global-interceptors :as global-interceptors]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [clojure-finance-api.infra.auth.jwt :as auth]
            [clojure-finance-api.infra.interceptors.user-interceptors :as user-i]
            [clojure-finance-api.infra.interceptors.login-interceptors :as login-i]
            [clojure-finance-api.infra.interceptors.bank-data-interceptors :as bank-i]))

(def raw-routes
  [;; --- Login ---
   ["/login" :post [(body-params/body-params) login-i/login] :route-name :action-login :public true]
   ["/auth/me" :get [login-i/get-current-user] :route-name :auth-me]
   ["/logout" :post [login-i/logout] :route-name :action-logout]

   ;; --- Users ---
   ["/users"     :get  [user-i/list-users-interceptor] :route-name :list-users :roles [:admin]]
   ["/users"     :post [(body-params/body-params) user-i/user-create-interceptor] :route-name :create-user :roles [:admin]]
   ["/users/:id" :get  [user-i/user-find-by-id-interceptor] :route-name :find-user-by-id]
   ["/users/:id" :put  [(body-params/body-params) user-i/user-update-interceptor] :route-name :update-user :roles [:admin]]
   ["/users/:id" :delete [user-i/user-delete-interceptor] :route-name :delete-user :roles [:admin]]

   ;; --- Bank Data ---
   ["/bank-data" :get  [bank-i/list-bank-data-interceptor] :route-name :list-bank-data :rls true]
   ["/bank-data" :post [(body-params/body-params) bank-i/bank-data-create-interceptor] :route-name :create-bank-data :rls true]
   ["/bank-data/user/:user-id" :get [bank-i/bank-data-find-by-user-id-interceptor] :route-name :find-bank-data-by-user :rls true]
   ["/bank-data/:id" :get    [bank-i/bank-data-find-by-id-interceptor] :route-name :find-bank-data-by-id :rls true]
   ["/bank-data/:id" :put    [(body-params/body-params) bank-i/bank-data-update-interceptor] :route-name :update-bank-data :rls true]
   ["/bank-data/:id" :delete [bank-i/bank-data-delete-interceptor] :route-name :delete-bank-data :rls true]])

(defn- wrap-auth-interceptor
  [routes]
  (map
    (fn [route]
      (let [[path method interceptors] route
            opts          (apply hash-map (drop 3 route))
            is-public?    (:public opts)
            rls?          (:rls opts)
            roles         (:roles opts)
            clean-opts    (dissoc opts :public :roles :rls)
            auth-chain
              (cond
                is-public?
                interceptors

                :else
                (let [base-chain [auth/auth-interceptor]]
                  (-> base-chain
                      (cond-> roles (conj (auth/authorize-roles roles)))
                      (cond-> rls? (conj global-interceptors/rls-interceptor))
                      (into interceptors))))
            ]

        (vec
          (concat
            [path method auth-chain]
            (mapcat identity clean-opts)))))
    routes))


(def routes
  (-> raw-routes
      wrap-auth-interceptor
      set
      route/expand-routes))