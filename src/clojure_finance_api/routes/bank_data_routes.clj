(ns clojure-finance-api.routes.bank-data-routes
  (:require
    [clojure-finance-api.http.bank_data_interceptors :as interceptors]
    [io.pedestal.http.body-params :as body-params]))

(def routes
  #{["/bank-data" :get  interceptors/list-bank-data-interceptor]
    ["/bank-data/:id" :get interceptors/bank-data-find-by-id-interceptor]
    ["/bank-data/user/:user-id" :get interceptors/bank-data-find-by-user-id-interceptor]
    ["/bank-data" :post [(body-params/body-params) interceptors/bank-data-create-interceptor]]
    ["/bank-data/:id" :put [(body-params/body-params) interceptors/bank-data-update-interceptor]]
    ["/bank-data/:id" :delete interceptors/bank-data-delete-interceptor]})
