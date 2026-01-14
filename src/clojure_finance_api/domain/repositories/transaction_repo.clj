(ns clojure-finance-api.domain.repositories.transaction-repo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-maps})

(def ^:private transaction-columns
  [:id :from_user :to_user :amount :status :created_at])

(def ^:private transaction-columns
  [:t.id :t.amount :t.status :t.created_at
   [:f.id :from_user_id] [:f.name :from_user_name] [:f.email :from_user_email]
   [:r.id :to_user_id] [:r.name :to_user_name] [:r.email :to_user_email]])

(defn find-transactions-by-user-id [ds user-id filters]
  (let [uuid (if (string? user-id) (java.util.UUID/fromString user-id) user-id)
        {:keys [type days]} filters]
    (jdbc/execute! ds
                   (sql/format
                     {:select transaction-columns
                      :from   [[:transactions :t]]
                      :join   [[:users :f] [:= :t.from_user :f.id]
                               [:users :r] [:= :t.to_user :r.id]]
                      :where  [:and
                               [:or [:= :t.from_user uuid] [:= :t.to_user uuid]]
                               (case type
                                 "debit"  [:= :t.from_user uuid]
                                 "credit" [:= :t.to_user uuid]
                                 nil)
                               (when days
                                 [:>= :t.created_at [:raw (str "now() - interval '" days " days'")]])]
                      :order-by [[:t.created_at :desc]]})
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