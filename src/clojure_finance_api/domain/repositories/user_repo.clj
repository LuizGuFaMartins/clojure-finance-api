(ns clojure-finance-api.domain.repositories.user-repo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]))

(def builder {:builder-fn rs/as-unqualified-kebab-maps})

(def base-user-query
  {:select [:u.* [:r.name :role]]
   :from [[:users :u]]
   :join [[:user_roles :ur] [:= :u.id :ur.user_id]
          [:roles :r] [:= :ur.role_id :r.id]]})

(defn list-users [ds & [{:keys [page size]}]]
  (let [limit  (or size 20)
        offset (* (dec (or page 1)) limit)
        query  (assoc base-user-query
                 :limit limit
                 :offset offset)]
    (jdbc/execute!
      ds
      (sql/format query)
      builder)))

(defn find-user-by-id [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      (merge base-user-query
             {:where [:= :u.id id]}))
    builder))

(defn find-user-by-email [ds email]
  (jdbc/execute-one!
    ds
    (sql/format
      (merge base-user-query
             {:where [:= :u.email email]}))
    builder))

(defn create-user! [ds user]
  (jdbc/with-transaction [tx ds]
   (let [inserted-user (jdbc/execute-one!
                         tx
                         (sql/format
                           {:insert-into :users
                            :values [(select-keys user [:id :name :email :password :cpf :phone])]
                            :returning [:*]})
                         builder)

         role-name (or (:role user) "customer")

         role-id-map (jdbc/execute-one!
                       tx
                       (sql/format {:select [:id]
                                    :from :roles
                                    :where [:= :name role-name]})
                       builder)]

     (if-not role-id-map
       (throw (ex-info "Role not found" {:role role-name}))

       (do
         (jdbc/execute!
           tx
           (sql/format
             {:insert-into :user_roles
              :values [{:user_id [:cast (:id inserted-user) :uuid]
                        :role_id (:id role-id-map) }]})
           builder)

         (assoc inserted-user :role role-name))))))

(defn update-user! [ds id data]
  (jdbc/execute-one!
    ds
    (sql/format
      {:update :users
       :set    (assoc (dissoc data :role) :updated-at :%now)
       :where  [:= :id [:cast id :uuid]]
       :returning [:*]})
    builder))

(defn delete-user! [ds id]
  (jdbc/execute-one!
    ds
    (sql/format
      {:delete-from :users
       :where [:= :id id]
       :returning [:id]})
    builder))