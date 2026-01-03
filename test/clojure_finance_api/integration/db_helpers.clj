(ns clojure-finance-api.integration.db-helpers
  (:require [next.jdbc :as jdbc]))

(defn clean-db!
  "Remove todos os dados das tabelas, mas mantém a estrutura.
   Útil para rodar no início ou fim de cada teste."
  [ds]
  (jdbc/with-transaction [tx ds]
     (jdbc/execute! tx ["SET session_replication_role = 'replica';"])
     (jdbc/execute! tx ["TRUNCATE TABLE users, bank_data RESTART IDENTITY CASCADE;"])
     (jdbc/execute! tx ["SET session_replication_role = 'origin';"])))