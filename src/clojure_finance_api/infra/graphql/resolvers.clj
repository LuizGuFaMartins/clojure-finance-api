(ns clojure-finance-api.infra.graphql.resolvers
  (:require [clojure-finance-api.domain.services.transaction-service :as service]
            [com.walmartlabs.lacinia.resolve :as lacinia]))

(defn resolve-error
  [error-data]
  (let [error-type (if (keyword? error-data) error-data (:error error-data))
        message (case error-type
                  :user-not-found       "Usuário não encontrado"
                  :insufficient-funds   "Saldo insuficiente para realizar a operação"
                  :invalid-amount       "O valor da transação é inválido"
                  :self-transfer        "Operação não permitida entre mesma conta"
                  :database-error       "Erro interno no processamento"
                  "Ocorreu um erro inesperado na transação")]
    (lacinia/resolve-as nil {:message message
                             :type error-type})))

(defn my-transactions [ctx args _]
  (let [result (service/my-transactions ctx args)]
    (if (:success result)
      (:success result)
      (resolve-error (:error result)))))

(defn all-transactions [ctx args _]
  (let [result (service/all-transactions ctx args)]
    (if (:success result)
      (:success result)
      (resolve-error (:error result)))))

(defn create-transaction [ctx args _]
  (let [result (service/create-transaction ctx (:input args))]
    (if (:success result)
      (:success result)
      (resolve-error (:error result)))))
