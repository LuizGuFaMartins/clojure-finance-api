(ns clojure-finance-api.infra.routes.login-routes
  (:require
    [clojure-finance-api.infra.interceptors.login-interceptors :as interceptors]
    [io.pedestal.http.body-params :as body-params]))

(def routes
  #{["/login" :post [(body-params/body-params) interceptors/login]]})
