(ns clojure-finance-api.shared.global-interceptors
  (:require
    [next.jdbc :as jdbc]
    [io.pedestal.interceptor :refer [interceptor]]
    [io.pedestal.http.cors :as cors]
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
            (let [user-id (get-in context [:request :auth :id])
                  datasource (get-in context [:request :components :datasource])]
              (if user-id
                (let [conn (jdbc/get-connection datasource)]
                  (jdbc/execute! conn [(str "SET LOCAL app.current_user_id = '" user-id "'")])
                  (assoc-in context [:request :tx-conn] conn))
                context)))

   :leave (fn [context]
            (when-let [conn (get-in context [:request :tx-conn])]
              (.close conn))
            context)

   :error (fn [context _]
            (when-let [conn (get-in context [:request :tx-conn])]
              (.close conn))
            context)})