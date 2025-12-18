(ns clojure-finance-api.infra.http.routes
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [clojure-finance-api.infra.auth.jwt :as auth]
            [clojure-finance-api.infra.interceptors.user-interceptors :as user-i]
            [clojure-finance-api.infra.interceptors.login-interceptors :as login-i]
            [clojure-finance-api.infra.interceptors.bank-data-interceptors :as bank-i]))

(def routes
  (route/expand-routes
    #{;; --- Login ---
      ["/login" :post [(body-params/body-params) login-i/login] :route-name :action-login]

      ;; --- Users ---
      ["/users"     :get  [auth/auth-interceptor user-i/list-users-interceptor] :route-name :list-users]
      ["/users"     :post [(body-params/body-params) user-i/user-create-interceptor] :route-name :create-user]
      ["/users/:id" :get  [auth/auth-interceptor user-i/user-find-by-id-interceptor] :route-name :find-user-by-id]
      ["/users/:id" :put  [(body-params/body-params) auth/auth-interceptor user-i/user-update-interceptor] :route-name :update-user]
      ["/users/:id" :delete [auth/auth-interceptor user-i/user-delete-interceptor] :route-name :delete-user]

      ;; --- Bank Data ---
      ;; Listagem e Criação
      ["/bank-data" :get  [auth/auth-interceptor bank-i/list-bank-data-interceptor] :route-name :list-bank-data]
      ["/bank-data" :post [(body-params/body-params) auth/auth-interceptor bank-i/bank-data-create-interceptor] :route-name :create-bank-data]

      ;; Busca por Usuário (Importante: vir antes do /:id se não usar linear-search, mas aqui a ordem no Set #{} não é garantida)
      ["/bank-data/user/:user-id" :get [auth/auth-interceptor bank-i/bank-data-find-by-user-id-interceptor] :route-name :find-bank-data-by-user]

      ;; Operações por ID
      ["/bank-data/:id" :get    [auth/auth-interceptor bank-i/bank-data-find-by-id-interceptor] :route-name :find-bank-data-by-id]
      ["/bank-data/:id" :put    [(body-params/body-params) auth/auth-interceptor bank-i/bank-data-update-interceptor] :route-name :update-bank-data]
      ["/bank-data/:id" :delete [auth/auth-interceptor bank-i/bank-data-delete-interceptor] :route-name :delete-bank-data]}))