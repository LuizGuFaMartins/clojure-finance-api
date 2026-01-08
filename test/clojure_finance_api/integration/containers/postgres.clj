(ns clojure-finance-api.integration.containers.postgres
  (:import (org.testcontainers.containers PostgreSQLContainer)
           (org.testcontainers.utility DockerImageName)))

;; FORÇA A VERSÃO DA API ANTES DE QUALQUER INICIALIZAÇÃO
(System/setProperty "DOCKER_API_VERSION" "1.44")

(defn create-pg-container []
  (let [image (DockerImageName/parse "postgres:16-alpine")
        container (doto (PostgreSQLContainer. image)
                    (.withDatabaseName "finance_db")
                    (.withUsername "finance_user")
                    (.withPassword "finance_pass")
                    (.withStartupTimeout (java.time.Duration/ofSeconds 90)))]
    (.start container)
    container))

(defn get-db-config [container]
  {:dbtype   "postgresql"
   :jdbcUrl  (.getJdbcUrl container)
   :user     (.getUsername container)
   :password (.getPassword container)})

(defn stop-container! [container]
  (when (and container (.isRunning container))
    (.stop container)))