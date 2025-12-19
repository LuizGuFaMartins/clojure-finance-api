(ns clojure-finance-api.domain.schemas.user-schemas)

(def UserCreateSchema
  [:map
   [:name string?]
   [:email string?]
   [:cpf string?]
   [:phone string?]
   [:password string?]])

(def UserIdSchema
  [:uuid])

