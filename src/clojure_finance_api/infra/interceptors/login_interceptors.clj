(ns clojure-finance-api.infra.interceptors.login-interceptors
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.infra.http.response :refer [response response-error]]
    [clojure-finance-api.domain.schemas.login-schemas :as schemas]
    [clojure-finance-api.domain.services.login-service :as login-service]))

(defn error-type-handler [result]
  (let [error-type (:error result)]
     (case error-type
       :user-not-found (response-error 404 "User not found")
       :invalid-password (response-error 401 "Invalid credentials")
       (response-error 500 "Internal Server Error"))))

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
                          :error (error-type-handler result)
                          (response-error 500 "Unknown error")))))))}))