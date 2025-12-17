(ns clojure-finance-api.domain.schemas.login-schemas)

(def LoginSchema
  [:map
   [:email string?]
   [:password string?]])

