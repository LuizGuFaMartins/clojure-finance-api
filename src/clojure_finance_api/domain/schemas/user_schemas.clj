(ns clojure-finance-api.domain.schemas.user-schemas)

(def UserCreateSchema
  [:map
   [:name string?]
   [:email string?]
   [:cpf string?]
   [:phone string?]
   [:password string?]])

(def UserUpdateSchema
  [:map
   [:name {:optional true} string?]
   [:email {:optional true} string?]
   [:cpf {:optional true} string?]
   [:phone {:optional true} string?]
   [:password {:optional true} string?]])

(def UserIdSchema
  [:uuid])

