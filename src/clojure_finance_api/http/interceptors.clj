(ns clojure-finance-api.http.interceptors
  (:require
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.services.user-service :as user-services]))

(def say-hello-interceptor
  (interceptor
    {:name ::say-hello
     :enter (fn [ctx]
              (let [resp (user-services/respond-hello (:request ctx))]
                (assoc ctx :response resp)))}))

(def user-create-interceptor
  (interceptor
    {:name ::user-create
     :enter (fn [ctx]
              (let [resp (user-services/create-user (:request ctx))]
                (assoc ctx :response resp)))}))

(def interceptors
  {::say-hello  say-hello-interceptor
   ::user-create user-create-interceptor})
