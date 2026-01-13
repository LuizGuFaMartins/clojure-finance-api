(ns clojure-finance-api.infra.http.routes
  (:require [clojure-finance-api.shared.global-interceptors :as global-interceptors]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.body-params :as body-params]
            [clojure-finance-api.infra.auth.jwt :as auth]
            [clojure-finance-api.infra.interceptors.user-interceptors :as user-i]
            [clojure-finance-api.infra.interceptors.login-interceptors :as login-i]
            [clojure-finance-api.infra.interceptors.bank-data-interceptors :as bank-i]
            [clojure-finance-api.infra.graphql.core :as gql-core]
            [com.walmartlabs.lacinia.pedestal2 :as lp]))

(def compiled-gql (gql-core/compiled-schema))

(def prepare-lacinia-context
  (interceptor
    {:name ::prepare-lacinia-context
     :enter (fn [ctx]
              (let [request (:request ctx)
                    components (:components ctx)]
                (assoc-in ctx [:request :lacinia-app-context]
                          {:components components
                           :request    request
                           })))}))


(defn- graphql-interceptors [schema]
  [prepare-lacinia-context
   (lp/inject-app-context-interceptor nil)
   lp/json-response-interceptor
   lp/error-response-interceptor
   lp/body-data-interceptor
   lp/graphql-data-interceptor
   lp/status-conversion-interceptor
   lp/missing-query-interceptor
   (lp/query-parser-interceptor schema)
   lp/prepare-query-interceptor
   lp/query-executor-handler])

(def raw-routes
  [;; --- Login & Public ---
   ["/login" :post [(body-params/body-params) login-i/login] :route-name :action-login :public true]

   ;; --- GraphQL & IDE ---
   ;; GraphiQL é público para facilitar o desenvolvimento/testes
   ;["/graphiql" :get [(lp/graphiql-ide-handler {})] :route-name :graphiql :public true]

   ;; A rota da API GraphQL herda a segurança JWT automaticamente (não é :public)
   ["/graphql" :post (graphql-interceptors compiled-gql) :route-name :graphql-api]

   ;; --- Auth & Session ---
   ["/auth/me" :get [login-i/get-current-user] :route-name :auth-me]
   ["/logout" :post [login-i/logout] :route-name :action-logout :public true]

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