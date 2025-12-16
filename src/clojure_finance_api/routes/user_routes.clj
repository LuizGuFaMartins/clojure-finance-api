(ns clojure-finance-api.routes.user-routes
  (:require
    [clojure-finance-api.http.user_interceptors :as interceptors]
    [io.pedestal.http.body-params :as body-params]))

(def routes
  #{["/users" :get interceptors/list-users-interceptor]
    ["/users/:id" :get interceptors/user-find-by-id-interceptor]
    ["/users" :post [(body-params/body-params) interceptors/user-create-interceptor]]
    ["/users/:id" :put [(body-params/body-params) interceptors/user-update-interceptor]]
    ["/users/:id" :delete interceptors/user-delete-interceptor]})
