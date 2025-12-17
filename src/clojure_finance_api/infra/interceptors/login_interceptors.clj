(ns clojure-finance-api.infra.interceptors.login-interceptors
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [cheshire.core :as json]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.domain.schemas.login-schemas :as schemas]
    [clojure-finance-api.infra.auth.auth :as auth]
    [clojure-finance-api.domain.services.user-service :as user-service]
    [clojure-finance-api.domain.services.login-service :as login-service]))

(defn response
  ([status]
   (response status nil))
  ([status body]
   (merge
     {:status status
      :headers {"Content-Type" "application/json"}}
     (when body {:body (json/encode body)}))))

(defn response-error
  ([status message]
   (response status {:error message}))
  ([status message details]
   (response status {:error message :details details})))

(def login
  (interceptor
    {:name ::login
     :enter
     (fn [ctx]
       (let [body (get-in ctx [:request :json-params])]
         (if-not (m/validate schemas/LoginSchema body)
           (assoc ctx :response (response-error 400 "Invalid login payload"
                                                (me/humanize (m/explain schemas/LoginSchema body))))

           (let [result (login-service/authenticate ctx body)]
             (assoc ctx :response
                        (case (some-> result keys first)
                          :success (response 200 (:success result))
                          :error   (let [error-type (:error result)]
                                     (case error-type
                                       :user-not-found (response-error 404 "User not found")
                                       :invalid-password (response-error 401 "Invalid credentials")
                                       (response-error 500 "Internal Server Error")))
                          (response-error 500 "Unknown error")))))))}))