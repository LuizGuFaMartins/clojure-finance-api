(ns clojure-finance-api.shared.global-interceptors
  (:require
    [io.pedestal.interceptor :refer [interceptor]]
    [io.pedestal.http.content-negotiation :as content-negotiation]))

(defn inject-components
  [components]
  (interceptor
    {:name ::inject-components
     :enter (fn [ctx]
              (assoc ctx :components components))}))

(def content-negotiation-interceptor
  (content-negotiation/negotiate-content ["application/json"]))

(def cors-interceptor
  (interceptor
    {:name ::cors
     :leave
     (fn [context]
       (update context :response
               merge
               {:headers
                {"Access-Control-Allow-Origin" "http://localhost:3000"
                 "Access-Control-Allow-Methods" "GET, POST, PUT, DELETE, OPTIONS"
                 "Access-Control-Allow-Headers" "Content-Type, Authorization"}}))}))