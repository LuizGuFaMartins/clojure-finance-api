(ns clojure-finance-api.infra.components.pedestal-component
    (:require
      [com.stuartsierra.component :as component]
      [io.pedestal.http :as http]
      [io.pedestal.http.route :as route]
      [clojure-finance-api.shared.global-interceptors :as interceptors]
      [clojure-finance-api.infra.routes.login-routes :as login-routes]
      [clojure-finance-api.infra.routes.user-routes :as user-routes]
      [clojure-finance-api.infra.routes.bank-data-routes :as bank-data-routes]))

(def all-routes
  (route/expand-routes
    (set (concat
           login-routes/routes
           user-routes/routes
           bank-data-routes/routes))))

(defrecord PedestalComponent [config datasource]
           component/Lifecycle

           (start [component]
                  (println "Starting PedestalComponent")
                  (let [server (-> {
                                    ::http/router :linear-search
                                    ::http/routes all-routes
                                    ::http/type :jetty
                                    ::http/join? false
                                    ::http/port (-> config :server :port)
                                    ::http/components {:datasource datasource}}
                                   (http/default-interceptors)
                                   (update ::http/interceptors concat
                                           [(interceptors/inject-components component)
                                            interceptors/content-negotiation-interceptor
                                            interceptors/cors-interceptor])
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
