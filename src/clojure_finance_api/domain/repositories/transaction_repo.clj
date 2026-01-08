(ns clojure-finance-api.domain.repositories.transaction-repo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-kebab-maps})

(defn list-transactions [ds]
  (jdbc/execute!
    ds
    (sql/format
      {:select [:id :user-id :card-holder :card-last-4 :card-brand
                :expires-month :expires-year :created-at]
       :from   :transactions})
    builder))

(defn find-transaction-by-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select [:id :user-id :card-holder :card-last-4 :card-brand
                :expires-month :expires-year :created-at]
       :from   :transactions
       :where  [:= :id id]})
    builder))

(defn find-transaction-by-user-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:select [:id :user-id :card-holder :card-last-4 :card-brand
                :expires-month :expires-year :created-at]
       :from   :transactions
       :where  [:= :user-id id]})
    builder))

(defn create-transaction! [ds transactions]
  (jdbc/execute-one!
    ds
    (sql/format
      {:insert-into :transactions
       :values [(select-keys transactions
                             [:id :user-id :card-holder :card-last-4
                              :card-hash :card-brand
                              :expires-month :expires-year])]
       :returning [:id :user-id :card-holder :card-last-4
                   :card-brand :expires-month :expires-year :created-at]})
    builder))

(defn update-transaction! [ds id data]
  (let [
        valid-data (select-keys data [:card-holder :card-last-4 :card-brand
                                      :expires-month :expires-year])

        query (sql/format
                {:update :transactions
                 :set    valid-data
                 :where  [:= :id id]
                 :returning [:id :user-id :card-holder :card-last-4
                             :card-brand :expires-month :expires-year :created-at]})]

    (try
      (jdbc/execute-one! ds query builder)
      (catch Exception e
        (let [msg (ex-message e)]
          (println "Erro detalhado no Update Bank Data:" msg)
          (throw (ex-info "Falha ao atualizar dados bancários"
                          {:cause msg :data valid-data})))))))

(defn delete-transaction! [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:delete-from :transactions
       :where [:= :id id]
       :returning [:id]})
    builder))
