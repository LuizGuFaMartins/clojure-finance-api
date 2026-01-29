(ns clojure-finance-api.infra.components.datasource-component
  (:require
    [com.stuartsierra.component :as component])
  (:import
    (com.zaxxer.hikari HikariConfig HikariDataSource)
    (org.flywaydb.core Flyway)))

(defn- build-jdbc-url [{:keys [host port dbname]}]
  (format "jdbc:postgresql://%s:%s/%s"
          host
          (if (string? port) (Integer/parseInt port) port)
          dbname))

(defn- run-flyway! [jdbc-url user password]
  (let [config (Flyway/configure)]
    (-> (if (and user password)
          (.dataSource config jdbc-url user password)
          (.dataSource config jdbc-url nil nil))
        (.load)
        (.migrate))))

(defn- hikari-datasource [jdbc-url user password statement-timeout]
  (let [cfg (doto (HikariConfig.)
              (.setJdbcUrl jdbc-url)
              (.setUsername user)
              (.setPassword password)

              ;; --- CONFIGURAÇÕES DE POOL ---
              (.setMaximumPoolSize 10)
              (.setConnectionTimeout 30000) ;; 30s para obter conexão do pool
              (.setMaxLifetime 1800000)     ;; 30min vida máxima da conexão

              ;; --- SEGURANÇA E PERFORMANCE ---
              ;; Aborta queries que excederem o tempo (ex: 10000ms)
              (.addDataSourceProperty "options" (str "-c statement_timeout=" statement-timeout))
              (.addDataSourceProperty "cachePrepStmts" "true")
              (.addDataSourceProperty "prepStmtCacheSize" "250")

              )]
    (HikariDataSource. cfg)))

(defrecord DatasourceComponent [config datasource]
  component/Lifecycle

  (start [this]
    (println "Starting DatasourceComponent")
    (let [db-config (:db config)
          jdbc-url  (build-jdbc-url db-config)
          user      (:user db-config)
          password  (:password db-config)
          ;; Timeout padrão de 15s para queries se não especificado
          s-timeout (or (:statement-timeout db-config) 15000)]

      (run-flyway! jdbc-url user password)

      (assoc this :datasource (hikari-datasource jdbc-url user password s-timeout))))

  (stop [this]
    (println "Stopping DatasourceComponent")
    (when-let [ds (:datasource this)]
      (.close ds))
    (assoc this :datasource nil)))

(defn new-datasource-component [config]
  (map->DatasourceComponent {:config config}))