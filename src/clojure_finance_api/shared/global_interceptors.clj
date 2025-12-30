(ns clojure-finance-api.shared.global-interceptors
  (:require
    [next.jdbc :as jdbc]
    [io.pedestal.interceptor :refer [interceptor]]
    [io.pedestal.http.ring-middlewares :as middlewares]
    [io.pedestal.http.content-negotiation :as content-negotiation]))

(defn inject-components
  [components]
  (interceptor
    {:name ::inject-components
     :enter (fn [ctx]
              (assoc ctx :components components))}))

(def content-negotiation-interceptor
  (content-negotiation/negotiate-content ["application/json"]))

(def cookies-interceptor middlewares/cookies)

(def rls-interceptor
  {:name :rls-interceptor
   :enter (fn [context]
            (let [user-id (get-in context [:request :identity :id])
                  datasource (get-in context [:components :datasource])]
              (if user-id
                (let [conn (jdbc/get-connection datasource)]
                  (.setAutoCommit conn false)
                  (try
                    (jdbc/execute! conn [(str "SET LOCAL app.current_user_id = '" user-id "'")])
                    (assoc-in context [:request :tx-conn] conn)
                    (catch Exception e
                      (.close conn)
                      (throw e))))
                context)))

   :leave (fn [context]
            (when-let [conn (get-in context [:request :tx-conn])]
              (.commit conn) ;; Finaliza com sucesso
              (.close conn))
            context)

   :error (fn [context err]
            (when-let [conn (get-in context [:request :tx-conn])]
              (.rollback conn) ;; Desfaz em caso de erro
              (.close conn))
            (assoc context :io.pedestal.interceptor.chain/error err))})