(ns clojure-finance-api.domain.repositories.transaction-repo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-kebab-maps})

(def ^:private transaction-columns
  [:id :from-user :to-user :amount :status :created-at])

(defn list-transactions [ds]
  (jdbc/execute!
    ds
    (sql/format
      {:select transaction-columns
       :from   :transactions})
    builder))

(defn find-transaction-by-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select transaction-columns
       :from   :transactions
       :where  [:= :id id]})
    builder))

(defn find-transactions-by-user-id [ds user-id]
  (let [uuid (if (string? user-id)
               (java.util.UUID/fromString user-id)
               user-id)]
    (jdbc/execute!
      ds
      (sql/format
        {:select transaction-columns
         :from   :transactions
         :where  [:or [:= :from-user uuid] [:= :to-user uuid]]
         :order-by [[:created-at :desc]]})
      builder)))

(defn create-transaction! [ds transaction-data]
  (jdbc/execute-one!
    ds
    (sql/format
      {:insert-into :transactions
       :values [(select-keys transaction-data [:from-user :to-user :amount :status])]
       :returning transaction-columns})
    builder))

(defn update-transaction! [ds id data]
  (let [valid-data (select-keys data [:amount :status])
        query (sql/format
                {:update :transactions
                 :set    valid-data
                 :where  [:= :id id]
                 :returning transaction-columns})]
    (try
      (jdbc/execute-one! ds query builder)
      (catch Exception e
        (let [msg (ex-message e)]
          (println "Erro detalhado no Update Transaction:" msg)
          (throw (ex-info "Falha ao atualizar transação"
                          {:cause msg :data valid-data})))))))

(defn delete-transaction! [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:delete-from :transactions
       :where [:= :id id]
       :returning [:id]})
    builder))