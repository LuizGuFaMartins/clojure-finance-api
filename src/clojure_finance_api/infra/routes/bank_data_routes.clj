(ns clojure-finance-api.infra.routes.bank-data-routes
  (:require
    [clojure-finance-api.infra.interceptors.bank_data_interceptors :as interceptors]
    [io.pedestal.http.body-params :as body-params]))

(def routes
  #{["/bank-data" :get interceptors/list-bank-data-interceptor :route-name :list-bank-data]
    ["/bank-data" :post [(body-params/body-params) interceptors/bank-data-create-interceptor] :route-name :create-bank-data]
    ["/bank-data/user/:user-id" :get interceptors/bank-data-find-by-user-id-interceptor :route-name :find-by-user]
    ["/bank-data/:id" :get interceptors/bank-data-find-by-id-interceptor :route-name :find-by-id]
    ["/bank-data/:id" :put [(body-params/body-params) interceptors/bank-data-update-interceptor] :route-name :update-bank-data]
    ["/bank-data/:id" :delete interceptors/bank-data-delete-interceptor :route-name :delete-bank-data]})
