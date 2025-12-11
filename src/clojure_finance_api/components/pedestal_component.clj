(ns clojure-finance-api.components.pedestal-component
    (:require
      [com.stuartsierra.component :as component]
      [io.pedestal.http :as http]
      [io.pedestal.http.route :as route]
      [clojure-finance-api.routes.user-routes :as user-routes]
      [clojure-finance-api.http.interceptors :as interceptors]))

(def all-routes
  (route/expand-routes
    (set (concat
           user-routes/routes))))

(defrecord PedestalComponent [config datasource]
           component/Lifecycle

           (start [component]
                  (println "Starting PedestalComponent")
                  (let [server (-> {::http/routes all-routes
                                    ::http/type :jetty
                                    ::http/join? false
                                    ::http/port (-> config :server :port)
                                    ::http/components {:datasource datasource}}
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
