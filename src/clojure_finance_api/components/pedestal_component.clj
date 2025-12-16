(ns clojure-finance-api.components.pedestal-component
    (:require
      [com.stuartsierra.component :as component]
      [io.pedestal.http :as http]
      [io.pedestal.http.route :as route]
      [io.pedestal.interceptor :refer [interceptor]]
      [clojure-finance-api.routes.user-routes :as user-routes]
      [clojure-finance-api.routes.bank-data-routes :as bank-data-routes]
      [io.pedestal.http.content-negotiation :as content-negotiation]))

(def all-routes
  (route/expand-routes
    (set (concat
           user-routes/routes
           bank-data-routes/routes))))

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

(defrecord PedestalComponent [config datasource]
           component/Lifecycle

           (start [component]
                  (println "Starting PedestalComponent")
                  (let [server (-> {::http/routes all-routes
                                    ::http/type :jetty
                                    ::http/join? false
                                    ::http/port (-> config :server :port)
                                    ::http/components {:datasource datasource}}
                                   (http/default-interceptors)
                                   (update ::http/interceptors concat
                                           [(inject-components component) content-negotiation-interceptor cors-interceptor])
                                   http/create-server
                                   http/start)]
                       (assoc component :server server)))

           (stop [component]
                 (println "Stopping PedestalComponent")
                 (when-let [server (:server component)]
                           (http/stop server))
                 (assoc component :server nil)))

(defn new-pedestal-component [config]
      (map->PedestalComponent {:config config}))
