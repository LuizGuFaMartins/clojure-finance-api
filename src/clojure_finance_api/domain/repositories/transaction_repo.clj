(ns clojure-finance-api.domain.repositories.transaction-repo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]))

(defn- column-reader [^java.sql.ResultSet rs _ ^Integer i]
  (let [value (.getObject rs i)]
    (if (instance? java.util.UUID value)
      (str value) ;; Converte no ato da leitura, sem loop extra
      value)))

(def builder {:builder-fn (rs/as-maps-with-adapter
                            rs/as-unqualified-kebab-maps
                            column-reader)})

(def ^:private transaction-columns
  [:id :from_user :to_user :amount :status :created_at])

(defn find-transactions-by-user-id [ds user-id filters]
  (let [uuid (if (string? user-id) (java.util.UUID/fromString user-id) user-id)
        {:keys [type days]} filters]
    (jdbc/execute! ds
       (sql/format
         {:select transaction-columns
          :from   :transactions
          :where  [:and
                   [:or [:= :from_user uuid] [:= :to_user uuid]]
                   (case type
                     "debit"  [:= :from_user uuid]
                     "credit" [:= :to_user uuid]
                     nil)
                   (when days
                     [:>= :created_at [:raw (str "now() - interval '" days " days'")]])]
          :order-by [[:created-at :desc]]})
       builder)))

(defn create-transaction! [ds {:keys [from-user-id to-user-id amount]}]
  (let [amt (bigdec amount)]
    (jdbc/with-transaction [tx ds]
       (let [from-updated (jdbc/execute-one! tx (sql/format {:update :users :set {:balance [:- :balance amt]}
                                                             :where [:and [:= :id from-user-id ] [:>= :balance amt]]}))]
         (if-not from-updated
           (throw (ex-info "Saldo insuficiente" {:type :insufficient-funds}))
           (let [to-updated (jdbc/execute-one! tx (sql/format {:update :users :set {:balance [:+ :balance amt]}
                                                               :where [:= :id to-user-id]}))]
             (if-not to-updated
               (throw (ex-info "Destinatário inválido" {:type :recipient-not-found}))
               (let [res (jdbc/execute-one! tx (sql/format {:insert-into :transactions
                                                            :values [{:from_user from-user-id  :to_user to-user-id
                                                                      :amount amt :status "completed"}]
                                                            :returning [:*]}) builder)]
                 (assoc res
                   :id (str (:id res))
                   :from_user (str (:from-user res))
                   :to_user   (str (:to-user res))
                   :amount    (double (:amount res)))))))))))