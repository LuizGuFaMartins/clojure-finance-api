(ns clojure-finance-api.http.bank_data_interceptors
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [malli.transform :as mt]
    [cheshire.core :as json]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.services.bank_data_service :as bank-data-service]))


;; Schemas
(def BankDataCreateSchema
  [:map
   [:user-id uuid?]
   [:card-holder string?]
   [:card-last4 [:re #"\d{4}"]]
   [:card-hash string?]
   [:card-brand string?]
   [:expires-month [:int {:min 1 :max 12}]]
   [:expires-year [:int {:min 2024}]]])

(def BankDataIdSchema
  [:uuid])

;; Interceptors
(defn response
  ([status]
   (response status nil))
  ([status body]
   (merge
     {:status status
      :headers {"Content-Type" "application/json"}}
     (when body {:body (json/encode body)}))))

(def json-transformer
  (mt/transformer
    mt/string-transformer
    mt/json-transformer))

(def list-bank-data-interceptor
  (interceptor
    {:name ::list-bank-data
     :enter
     (fn [ctx]
       (let [bank-data (bank-data-service/list-bank-data ctx)]
         (assoc ctx :response (response 200 bank-data))))}))

(def bank-data-find-by-id-interceptor
  (interceptor
    {:name ::bank-data-find-by-id
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])
             id     (parse-uuid id-str)]

         (if-not (m/validate BankDataIdSchema id)
           (assoc ctx :response (response 400 {:error "Invalid bank-data id"}))

           (if-let [bank-data (bank-data-service/find-bank-data-by-id ctx id)]
             (assoc ctx :response (response 200 bank-data))
             (assoc ctx :response (response 404 {:error "BankData not found"}))))))}))

(def bank-data-find-by-user-id-interceptor
  (interceptor
    {:name ::bank-data-find-by-user-id
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :user-id])
             id     (parse-uuid id-str)]

         (if-not (m/validate BankDataIdSchema id)
           (assoc ctx :response (response 400 {:error "Invalid user id"}))

           (if-let [bank-data (bank-data-service/find-bank-data-by-user-id ctx id)]
             (assoc ctx :response (response 200 bank-data))
             (assoc ctx :response (response 404 {:error "BankData not found"}))))))}))

(def bank-data-create-interceptor
  (interceptor
    {:name ::bank-data-create
     :enter
     (fn [ctx]
       (let [raw-body (get-in ctx [:request :json-params])
             body     (m/decode BankDataCreateSchema
                                raw-body
                                json-transformer)]

         (if-not (m/validate BankDataCreateSchema body)
           (assoc ctx :response
                      (response 400
                                {:error "Invalid bank-data payload"
                                 :details (me/humanize
                                            (m/explain BankDataCreateSchema body))}))

           (let [bank-data (bank-data-service/create-bank-data ctx body)]
             (assoc ctx :response
                        (response 201 bank-data))))))}))

(def bank-data-update-interceptor
  (interceptor
    {:name ::bank-data-update
     :enter
     (fn [ctx]
       (let [id   (some-> ctx :request :path-params :id parse-uuid)
             body (get-in ctx [:request :json-params])]
         (if-let [bank-data (bank-data-service/update-bank-data ctx id body)]
           (assoc ctx :response (response 200 bank-data))
           (assoc ctx :response
                      (response 404 {:error "BankData not found"})))))}))

(def bank-data-delete-interceptor
  (interceptor
    {:name ::bank-data-delete
     :enter
     (fn [ctx]
       (let [id (some-> ctx :request :path-params :id parse-uuid)]
         (if (bank-data-service/delete-bank-data ctx id)
           (assoc ctx :response (response 204))
           (assoc ctx :response
                      (response 404 {:error "Bank data not found"})))))}))
