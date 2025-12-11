(ns clojure-finance-api.core
  (:require
    [clojure-finance-api.components.datasource-component :as datasource-component]
    [clojure-finance-api.components.pedestal-component :as pedestal-component]
    [clojure-finance-api.config :as config]
    [com.stuartsierra.component :as component]))

(defn clojure-finance-api-system
  [config]
      (component/system-map
        :datasource (datasource-component/new-datasource-component config)
        :pedestal-component (component/using (pedestal-component/new-pedestal-component config) [:datasource]))
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