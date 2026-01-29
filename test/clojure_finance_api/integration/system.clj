(ns clojure-finance-api.integration.system
  (:require [clojure-finance-api.core :as core]))

(defn test-system
  "Cria o sistema real da aplicação injetando a config do container."
  [db-config]
  (let [
        config {:server {:port 0}
                :db     db-config
                :auth   {:jwt {:secret "test-secret-key-123"}}}]
    (core/clojure-finance-api-system config)))

(defn get-url
  "Extrai a porta do componente :pedestal-component definido no seu system-map."
  [system path]
  (let [port (-> system :pedestal-component :service-conf :io.pedestal.http/port)]
    (str "http://localhost:" port path)))