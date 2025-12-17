(ns clojure-finance-api.domain.schemas.user-schemas)

(def UserCreateSchema
  [:map
   [:name string?]
   [:email string?]
   [:password string?]])

(def UserIdSchema
  [:uuid])

