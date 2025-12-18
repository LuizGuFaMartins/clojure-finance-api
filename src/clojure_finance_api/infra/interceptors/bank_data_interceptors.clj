(ns clojure-finance-api.infra.interceptors.bank-data-interceptors
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [malli.transform :as mt]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.infra.http.response :refer [response response-error]]
    [clojure-finance-api.domain.schemas.bank-data-schemas :as schemas]
    [clojure-finance-api.domain.services.bank-data-service :as bank-data-service]))

(defn error-type-handler [result]
  (let [error-type (:error result)]
    (case error-type
      :bank-data-not-found (response-error 404 "Bank data not found")
      :user-not-found      (response-error 404 "User not found")
      :database-error      (response-error 500 "Internal database error")
      :invalid-id          (response-error 400 "Invalid UUID format")
      (response-error 500 "Internal Server Error"))))

(def json-transformer
  (mt/transformer mt/string-transformer mt/json-transformer))

(def list-bank-data-interceptor
  (interceptor
    {:name ::list-bank-data
     :enter
     (fn [ctx]
       (let [result (bank-data-service/list-bank-data ctx)]
         (assoc ctx :response
                    (cond
                      (:success result) (response 200 (:success result))
                      (:error result)   (error-type-handler result)
                      :else             (response-error 500 "Unknown error")))))}))

(def bank-data-find-by-id-interceptor
  (interceptor
    {:name ::bank-data-find-by-id
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])
             id     (some-> id-str parse-uuid)]
         (cond
           (nil? id)
           (assoc ctx :response (response-error 400 "Invalid bank-data id format"))

           :else
           (let [result (bank-data-service/find-bank-data-by-id ctx id)]
             (assoc ctx :response
                        (cond
                          (:success result) (response 200 (:success result))
                          (:error result)   (error-type-handler result)
                          :else             (response-error 500 "Unknown error")))))))}))

(def bank-data-find-by-user-id-interceptor
  (interceptor
    {:name ::bank-data-find-by-user-id
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :user-id])
             id     (some-> id-str parse-uuid)]
         (cond
           (nil? id)
           (assoc ctx :response (response-error 400 "Invalid user id format"))

           :else
           (let [result (bank-data-service/find-bank-data-by-user-id ctx id)]
             (assoc ctx :response
                        (cond
                          (:success result) (response 200 (:success result))
                          (:error result)   (error-type-handler result)
                          :else             (response-error 500 "Unknown error")))))))}))

(def bank-data-create-interceptor
  (interceptor
    {:name ::bank-data-create
     :enter
     (fn [ctx]
       (let [raw-body (get-in ctx [:request :json-params])
             body     (m/decode schemas/BankDataCreateSchema raw-body json-transformer)]
         (if-not (m/validate schemas/BankDataCreateSchema body)
           (assoc ctx :response
                      (response-error 400 "Invalid bank-data payload"
                                      (me/humanize (m/explain schemas/BankDataCreateSchema body))))
           (let [result (bank-data-service/create-bank-data ctx body)]
             (assoc ctx :response
                        (cond
                          (:success result) (response 201 (:success result))
                          (:error result)   (error-type-handler result)
                          :else             (response-error 500 "Unknown error")))))))}))

(def bank-data-update-interceptor
  (interceptor
    {:name ::bank-data-update
     :enter
     (fn [ctx]
       (let [id-str   (get-in ctx [:request :path-params :id])
             id       (some-> id-str parse-uuid)
             raw-body (get-in ctx [:request :json-params])
             body     (m/decode schemas/BankDataCreateSchema raw-body json-transformer)]
         (cond
           (nil? id)
           (assoc ctx :response (response-error 400 "Invalid bank-data id"))

           (not (m/validate schemas/BankDataCreateSchema body))
           (assoc ctx :response
                      (response-error 400 "Invalid update payload"
                                      (me/humanize (m/explain schemas/BankDataCreateSchema body))))

           :else
           (let [result (bank-data-service/update-bank-data ctx id body)]
             (assoc ctx :response
                        (cond
                          (:success result) (response 200 (:success result))
                          (:error result)   (error-type-handler result)
                          :else             (response-error 500 "Unknown error")))))))}))

(def bank-data-delete-interceptor
  (interceptor
    {:name ::bank-data-delete
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])
             id     (some-> id-str parse-uuid)]
         (if-not id
           (assoc ctx :response (response-error 400 "Invalid bank-data id"))
           (let [result (bank-data-service/delete-bank-data ctx id)]
             (assoc ctx :response
                        (cond
                          (:success result) (response 204)
                          (:error result)   (error-type-handler result)
                          :else             (response-error 500 "Unknown error")))))))}))