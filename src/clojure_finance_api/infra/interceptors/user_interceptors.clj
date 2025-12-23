(ns clojure-finance-api.infra.interceptors.user-interceptors
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.infra.http.response :refer [response response-error]]
    [clojure-finance-api.domain.schemas.user-schemas :as schemas]
    [clojure-finance-api.domain.services.user-service :as user-service]))

(defn error-type-handler [result]
  (let [error-type (:error result)]
    (case error-type
      :user-not-found    (response-error 404 "User not found")
      :invalid-password  (response-error 401 "Invalid credentials")
      :has-no-users      (response-error 404 "No users found")
      :email-already-in-use (response-error 409 "Email already registered")
      :database-error    (response-error 500 "Internal database error")
      (response-error 500 "Internal Server Error"))))

(def list-users-interceptor
  (interceptor
    {:name ::list-users
     :enter
     (fn [ctx]
       (let [result (user-service/list-users ctx)]
         (assoc ctx :response (case (some-> result keys first)
                                :success (response 200 (:success result))
                                :error (error-type-handler result)
                                (response-error 500 "Internal Server Error")))))}))

(def user-find-by-id-interceptor
  (interceptor
    {:name ::user-find-by-id
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])]
         (if (nil? (parse-uuid id-str))
           (assoc ctx :response (response-error 400 "Invalid UUID format"))

           (let [id     (parse-uuid id-str)
                 result (user-service/find-user-by-id ctx id)]
             (assoc ctx :response
                        (case (some-> result keys first)
                          :success (response 200 (:success result))
                          :error   (error-type-handler result)
                          (response-error 500 "Unknown error")))))))}))

(def user-create-interceptor
  (interceptor
    {:name ::user-create
     :enter
     (fn [ctx]
       (let [body (get-in ctx [:request :json-params])]
         (if-not (m/validate schemas/UserCreateSchema body)
           (assoc ctx :response (response-error 400 "Invalid user payload"
                                                (me/humanize (m/explain schemas/UserCreateSchema body))))

           (let [result (user-service/create-user ctx body)]
             (assoc ctx :response
                        (case (some-> result keys first)
                          :success (response 201 (:success result))
                          :error   (error-type-handler result)
                          (response-error 500 "Unknown error")))))))}))

(def user-update-interceptor
  (interceptor
    {:name ::user-update
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])
             id     (parse-uuid id-str)
             body   (get-in ctx [:request :json-params])]

         (cond
           (nil? id)
           (assoc ctx :response (response-error 400 "Invalid user id"))

           (not (m/validate schemas/UserCreateSchema body))
           (assoc ctx :response (response-error 400 "Invalid update payload"
                                                (me/humanize (m/explain schemas/UserCreateSchema body))))

           :else
           (let [result (user-service/update-user ctx id body)]
             (assoc ctx :response
                        (case (some-> result keys first)
                          :success (response 200 (:success result))
                          :error   (error-type-handler result)
                          (response-error 500 "Unknown error")))))))}))

(def user-delete-interceptor
  (interceptor
    {:name ::user-delete
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])
             id     (parse-uuid id-str)
             result (user-service/delete-user ctx id)]
         (assoc ctx :response
                    (case (some-> result keys first)
                      :success (response 204 nil)
                      :error   (error-type-handler result)
                      (response-error 500 "Unknown error")))))}))
