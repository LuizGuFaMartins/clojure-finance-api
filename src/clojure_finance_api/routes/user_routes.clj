(ns clojure-finance-api.routes.user-routes
  (:require
    [clojure-finance-api.http.interceptors :as interceptors]))

(def routes
  #{["/greet" :get interceptors/say-hello-interceptor]
    ["/users" :post interceptors/user-create-interceptor]})
