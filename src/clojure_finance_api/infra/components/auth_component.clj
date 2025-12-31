(ns clojure-finance-api.infra.components.auth-component
  (:require [com.stuartsierra.component :as component]))

(defrecord AuthComponent [config]
  component/Lifecycle
  (start [this]
    (println "Starting AuthComponent")
    (let [secret (get-in config [:auth :jwt :secret])]
      (assoc this :secret secret)))
  (stop [this]
    (println "Stopping AuthComponent")
    (assoc this :secret nil)))

(defn new-auth-component [config]
  (map->AuthComponent {:config config}))
