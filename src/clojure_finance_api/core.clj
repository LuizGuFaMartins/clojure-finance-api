(ns clojure-finance-api.core
  (:require
    [clojure-finance-api.infra.components.datasource-component :as datasource-component]
    [clojure-finance-api.infra.components.auth-component :as auth-component]
    [clojure-finance-api.infra.components.pedestal-component :as pedestal-component]
    [clojure-finance-api.config :as config]
    [com.stuartsierra.component :as component]))

(defn clojure-finance-api-system
  [config]
      (component/system-map
        :auth (auth-component/new-auth-component config)
        :datasource (datasource-component/new-datasource-component config)
        :pedestal-component (component/using (pedestal-component/new-pedestal-component config) [:datasource :auth]))
   )

(defn -main
  []
  (let [system (-> (config/read-config)
                   (clojure-finance-api-system)
                   (component/start-system))]

    (println "Starting Clojure Finance API Service")

    (.addShutdownHook
     (Runtime/getRuntime)
     (new Thread #(component/stop-system system)))))