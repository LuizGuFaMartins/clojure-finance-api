(ns clojure-finance-api.domain.services.transaction-service
  (:require
    [clojure-finance-api.domain.repositories.transaction-repo :as repo]
    [clojure-finance-api.domain.services.user-service :as user-service]
    [clojure-finance-api.infra.http.context-utils :as ctx-utils]))

(defn my-transactions [ctx args]
  (let [ds      (ctx-utils/get-db ctx)
        user-id (ctx-utils/get-user-id ctx)]
    (try
      {:success (repo/find-transactions-by-user-id ds user-id args)}
      (catch Exception e
        (println "Erro ao listar transações:" (.getMessage e))
        {:error :database-error}))))

(defn all-transactions [ctx args]
  (let [ds (ctx-utils/get-db ctx)]
    (try
      {:success (repo/find-transactions ds args)}
      (catch Exception e
        (println "Erro ao listar transações:" (.getMessage e))
        {:error :database-error}))))

(defn create-transaction [ctx input]
  (let [ds           (ctx-utils/get-db ctx)
        from-user-id (java.util.UUID/fromString (ctx-utils/get-user-id ctx))
        to-user-id   (java.util.UUID/fromString (:to_user input))
        amount       (:amount input)]
    (cond
      (<= (or amount 0) 0)
      {:error :invalid-amount}

      (= (str from-user-id) (str to-user-id))
      {:error :self-transfer}

      :else
      (let [recipient-res (user-service/find-user-by-id ctx to-user-id)]
        (if (:error recipient-res)
          {:error :user-not-found}

          (let [sender-res (user-service/find-user-by-id ctx from-user-id)
                sender     (:success sender-res)]
            (if (< (:balance sender) amount)
              {:error :insufficient-funds}

              (try
                (let [tx (repo/create-transaction! ds
                                                   {:from-user-id from-user-id
                                                    :to-user-id   to-user-id
                                                    :amount       amount})]
                  {:success tx})
                (catch Exception e
                  (let [data (ex-data e)]
                    {:error (or (:type data) :database-error)}))))))))))