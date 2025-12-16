(ns clojure-finance-api.http.user_interceptors
  (:require
    [malli.core :as m]
    [cheshire.core :as json]
    [io.pedestal.interceptor :refer [interceptor]]
    [clojure-finance-api.services.user-service :as user-service]))


;; Schemas
(def UserCreateSchema
  [:map
   [:name string?]
   [:email string?]
   [:password string?]])

(def UserIdSchema
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

(def list-users-interceptor
  (interceptor
    {:name ::list-users
     :enter
     (fn [ctx]
       (let [users (user-service/list-users ctx)]
         (assoc ctx :response (response 200 users))))}))

(def user-find-by-id-interceptor
  (interceptor
    {:name ::user-find-by-id
     :enter
     (fn [ctx]
       (let [id-str (get-in ctx [:request :path-params :id])
             id     (parse-uuid id-str)]

         (if-not (m/validate UserIdSchema id)
           (assoc ctx :response (response 400 {:error "Invalid user id"}))

           (if-let [user (user-service/find-user-by-id ctx id)]
             (assoc ctx :response (response 200 user))
             (assoc ctx :response (response 404 {:error "User not found"}))))))}))

(def user-create-interceptor
  (interceptor
    {:name ::user-create
     :enter
     (fn [ctx]
       (let [body (get-in ctx [:request :json-params])]

         (if-not (m/validate UserCreateSchema body)
           (assoc ctx :response (response 400 {:error "Invalid user payload"
                                               :details (m/explain UserCreateSchema body)}))

           (let [user (user-service/create-user ctx body)]
             (assoc ctx :response (response 201 user))))))}))

(def user-update-interceptor
  (interceptor
    {:name ::user-update
     :enter
     (fn [ctx]
       (let [id   (some-> ctx :request :path-params :id parse-uuid)
             body (get-in ctx [:request :json-params])]
         (if-let [user (user-service/update-user ctx id body)]
           (assoc ctx :response (response 200 user))
           (assoc ctx :response
                      (response 404 {:error "User not found"})))))}))

(def user-delete-interceptor
  (interceptor
    {:name ::user-delete
     :enter
     (fn [ctx]
       (let [id (some-> ctx :request :path-params :id parse-uuid)]
         (if (user-service/delete-user ctx id)
           (assoc ctx :response (response 204))
           (assoc ctx :response
                      (response 404 {:error "User not found"})))))}))
