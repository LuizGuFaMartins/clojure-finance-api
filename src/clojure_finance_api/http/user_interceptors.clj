(ns clojure-finance-api.http.user_interceptors
  (:require
    [malli.core :as m]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.services.user-service :as user-service]))

(def UserCreateSchema
  [:map
   [:name string?]
   [:email string?]
   [:password string?]])

(def list-users-interceptor
  (interceptor
    {:name ::list-users
     :enter (fn [ctx]
              (let [resp (user-service/list-users ctx)]
                (assoc ctx :response resp)))}))

(def user-create-interceptor
  (interceptor
    {:name ::user-create
     :enter
     (fn [ctx]
       (let [body (get-in ctx [:request :json-params])]

         (if-not (m/validate UserCreateSchema body)
           (assoc ctx :response
                      {:status 400
                       :body {:error "Invalid user payload"
                              :details (m/explain UserCreateSchema body)}})

           (let [resp (user-service/create-user ctx body)]
             (assoc ctx :response resp)))))}))
